package kiinse.me.zonezero.plugin;

import io.sentry.Sentry;
import kiinse.me.zonezero.plugin.apiserver.PlayersService;
import kiinse.me.zonezero.plugin.apiserver.ServerService;
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData;
import kiinse.me.zonezero.plugin.commands.core.CommandManager;
import kiinse.me.zonezero.plugin.commands.zonezero.*;
import kiinse.me.zonezero.plugin.commands.zonezero.tabcomplete.*;
import kiinse.me.zonezero.plugin.enums.Config;
import kiinse.me.zonezero.plugin.enums.Replace;
import kiinse.me.zonezero.plugin.enums.Strings;
import kiinse.me.zonezero.plugin.listeners.*;
import kiinse.me.zonezero.plugin.schedulers.core.SchedulersManager;
import kiinse.me.zonezero.plugin.schedulers.zonezero.PublicKeyScheduler;
import kiinse.me.zonezero.plugin.service.ApiConnection;
import kiinse.me.zonezero.plugin.service.LogFilter;
import kiinse.me.zonezero.plugin.service.data.ServerAnswer;
import kiinse.me.zonezero.plugin.service.interfaces.ApiService;
import kiinse.me.zonezero.plugin.utils.FilesUtils;
import kiinse.me.zonezero.plugin.utils.MessageUtils;
import kiinse.me.zonezero.plugin.utils.VersionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Utility;
import org.bukkit.plugin.java.JavaPlugin;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ZoneZero extends JavaPlugin {

    private final FilesUtils filesUtils = new FilesUtils(this);
    private TomlParseResult configuration = filesUtils.getTomlFile(Strings.CONFIG_FILE.getValue());
    private final MessageUtils messageUtils = new MessageUtils(filesUtils);
    private TomlTable toolsTable = configuration.getTableOrEmpty(Config.TABLE_TOOLS.getValue());
    private String token = configuration.getTableOrEmpty(Config.TABLE_CREDENTIALS.getValue()).getString(Config.CREDENTIALS_TOKEN.getValue(), () -> "");
    private TomlTable settingsTable = configuration.getTableOrEmpty(Config.TABLE_SETTINGS.getValue());
    private ApiService apiConnection = new ApiConnection(this, toolsTable);
    private PlayersData playersData = new PlayersService(this, apiConnection, settingsTable);
    private ServerService serverService = new ServerService(apiConnection);
    private SchedulersManager schedulersManager = new SchedulersManager();
    private static Boolean isDebug = false;
    private ServerAnswer serverAnswer = null;

    public void onReload() {
        try {
            sendInFrame(new ArrayList<>(){{
                add("&dReloading &f" + getName() + "&a...");
            }});
            playersData.savePlayersStatuses();
            configuration = filesUtils.getTomlFile(Strings.CONFIG_FILE.getValue());
            apiConnection = new ApiConnection(this, toolsTable);
            serverService = new ServerService(apiConnection);
            playersData = new PlayersService(this, apiConnection, settingsTable);
            settingsTable = configuration.getTableOrEmpty(Config.TABLE_SETTINGS.getValue());
            messageUtils.reload();
            toolsTable = configuration.getTableOrEmpty(Config.TABLE_TOOLS.getValue());
            token = toolsTable.getString(Config.CREDENTIALS_TOKEN.getValue(), () -> "");
            loadVariables();
            getServer().getPluginManager().disablePlugin(this);
            getServer().getPluginManager().enablePlugin(this);
            sendInFrame(new ArrayList<>(){{
                add("&f" + getName() + " &areloaded!");
            }});
        } catch (Exception e) {
            Sentry.captureException(e);
            sendLog(Level.SEVERE, "Error on loading " + getName() + "! Message:", e);
        }
    }

    @Override
    public void onLoad() {
        try {
            getLogger().setLevel(Level.CONFIG);
            ((Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
            loadVariables();
            setupSentry();
        } catch (Exception e) {
            sendLog(Level.SEVERE, "Error on loading " + getName() + "! Message:", e);
            Sentry.captureException(e);
        }
    }

    @Override
    public void onEnable() {
        checks(() -> {
            sendInFrame(new ArrayList<>(){{
                add("&aLoading &f" + getName() + "&a...");
            }});
            schedulersManager = new SchedulersManager();
            schedulersManager.register(new PublicKeyScheduler(this, apiConnection));
            var pluginManager = getServer().getPluginManager();
            pluginManager.registerEvents(new MoveListener(playersData), this);
            pluginManager.registerEvents(new InteractListener(playersData), this);
            pluginManager.registerEvents(new DamageListener(playersData), this);
            pluginManager.registerEvents(new MiningListener(playersData), this);
            pluginManager.registerEvents(new JoinListener(playersData, settingsTable, messageUtils), this);
            pluginManager.registerEvents(new QuitListener(playersData, settingsTable, messageUtils), this);
            pluginManager.registerEvents(new ExitListener(playersData), this);
            pluginManager.registerEvents(new ChatListener(playersData, settingsTable), this);
            pluginManager.registerEvents(new CommandsListener(playersData, messageUtils, settingsTable), this);
            new CommandManager(this)
                    .registerCommand(new LoginCommand(this, playersData, settingsTable))
                    .registerCommand(new RegisterCommand(this, playersData))
                    .registerCommand(new ZoneZeroCommand(this))
                    .registerCommand(new ChangePasswordCommand(this, playersData))
                    .registerCommand(new TwoFactorAuthCommand(this, playersData));
            Objects.requireNonNull(getCommand("login")).setTabCompleter(new LoginTab());
            Objects.requireNonNull(getCommand("register")).setTabCompleter(new RegisterTab());
            Objects.requireNonNull(getCommand("zonezero")).setTabCompleter(new ZoneZeroTab());
            Objects.requireNonNull(getCommand("changepassword")).setTabCompleter(new ChangePasswordTab());
            Objects.requireNonNull(getCommand("2fa")).setTabCompleter(new TwoFactorTab());

            var description = getDescription();
            sendInFrame(new ArrayList<>(){{
                add("&f" + getName() + " &aloaded!");
                add("&bAuthors: &f" + description.getAuthors());
                add("&bWebsite: &f" + description.getWebsite());
                add("&bPlugin version: &f" + description.getVersion());
            }});
        }, "Error on loading " + getName() + "! Message:");
    }

    @Override
    public void onDisable() {
        checks(() -> {
            sendInFrame(new ArrayList<>(){{
                add("&cDisabling &f" + getName() + "&a...");
            }});

            playersData.savePlayersStatuses();
            schedulersManager.stopSchedulers();

            sendInFrame(new ArrayList<>(){{
                add("&f" + getName() + " &cdisabled!");
            }});
        }, "Error on disabling " + getName() + "! Message:");
    }

    public String getToken() {
        return token;
    }

    public MessageUtils getMessageUtils() {return messageUtils;}
    public FilesUtils getFilesUtils() {return filesUtils;}

    public TomlParseResult getConfiguration() {
        return configuration;
    }

    private void checks(Runnable runnable, String onError) {
        try {
            apiConnection.updateServerKey();
            if (serverAnswer == null || serverAnswer.getStatus() != 200) serverAnswer = serverService.getPluginCode(this);
            var status = serverAnswer.getStatus();
            if (status == 200) {
                sendInFrame(new ArrayList<>(){{
                    add("&aRegister your server on &bhttps://zonezero.dev/");
                    add("");
                    add("&aYour server code is '&b" + serverAnswer.getData().getString(Strings.STRING_MESSAGE.getValue()) + "&a'");
                    add("");
                    add("&aEnter this code on &bhttps://zonezero.dev/servers/register");
                }});
            } else if (status == 403) {
                sendInFrame(new ArrayList<>(){{
                    add(Strings.SERVER_CORE_NOT_ALLOWED.getValue());
                    add("&c" + serverAnswer.getData().getString(Strings.STRING_MESSAGE.getValue()));
                }});
            } else if (status == 406) {
                runAsync(this::checkVersion);
                runAsync(runnable);
            } else {
                sendInFrame(new ArrayList<>(){{
                    add(Strings.ERROR_ON_GETTING_CODE.getValue());
                }});
            }
        } catch (Exception e) {
            sendLog(Level.SEVERE, onError, e);
            Sentry.captureException(e);
        }
    }

    private void setupSentry() {
        Sentry.init(options -> {
            options.setDsn(Strings.SENTRY_TOKEN.getValue());
            options.setTracesSampleRate(1.0);
            options.setDebug(false);
            options.setRelease(getDescription().getVersion());
        });
    }

    private void checkVersion() {
        try {
            VersionUtils.INSTANCE.getLatestSpigotVersion(latest -> {
                try {
                    if (latest.isGreaterThan(VersionUtils.INSTANCE.getPluginVersion(this))) {
                        var reader = new BufferedReader(new InputStreamReader(
                                Objects.requireNonNull(this.getClass()
                                        .getClassLoader()
                                        .getResourceAsStream(Strings.VERSION_FILE.getValue()))));
                        var builder = new StringBuilder("\n");
                        while (reader.ready()) {
                            builder.append(reader.readLine()).append("\n");
                        }
                        sendConsole(builder.toString()
                                .replace(Replace.VERSION_NEW.getValue(), latest.getOriginalValue())
                                .replace(Replace.VERSION_CURRENT.getValue(), getDescription().getVersion()));
                    } else {
                        sendInFrame(new ArrayList<>(){{
                            add(Strings.LATEST_VERSION.getValue());
                        }});
                    }
                } catch (Exception e) {
                    sendInFrame(new ArrayList<>(){{
                        add(Strings.PLUGIN_VERSION_CHECK_ERROR.getValue());
                        add(Strings.PREFIX_MESSAGE.getValue() + e.getMessage());
                    }});
                }
            });
        } catch (Exception e) {
            sendInFrame(new ArrayList<>(){{
                add(Strings.PLUGIN_VERSION_CHECK_ERROR.getValue());
                add(Strings.PREFIX_MESSAGE.getValue() + e.getMessage());
            }});
        }
    }

    private void loadVariables() {
        isDebug = toolsTable.getBoolean(Config.TOOLS_IS_DEBUG.getValue(), () -> false );
    }

    public static void sendLog(Level level, String message, Throwable throwable) {
        sendLog(level, message + " " + throwable.getMessage());
        if (isDebug) throwable.printStackTrace();
    }

    public static void sendLog(Level level, Throwable throwable) {
        sendLog(level, throwable.getMessage());
        if (isDebug) throwable.printStackTrace();
    }

    public static void sendLog(Level level, String msg) {
        runAsync(() -> {
            if (level.equals(Level.INFO)) {
                sendConsole("&6[&bZoneZero&6]&a " + msg);
            } else if (level.equals(Level.WARNING) || level.equals(Level.SEVERE)) {
                sendConsole("&6[&bZoneZero&f/&c" + level + "&6] " + msg);
            } else {
                if (isDebug) sendConsole("&6[&bZoneZero&f/&dDEBUG&6] " + msg);
            }
        });
    }

    public static void sendInFrame(List<String> list) {
        runAsync(() -> {
            sendConsole(" &6|==============================");
            for (var it : list) sendConsole(" &6|  " + it);
            sendConsole(" &6|==============================");
        });
    }

    private static void runAsync(Runnable runnable) {
        new Thread(runnable).start();
    }

    @Utility
    public static void sendConsole(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
package kiinse.me.zonezero.plugin;

import io.sentry.Sentry;
import kiinse.me.zonezero.plugin.apiserver.PlayersService;
import kiinse.me.zonezero.plugin.apiserver.ServerService;
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData;
import kiinse.me.zonezero.plugin.commands.zonezero.*;
import kiinse.me.zonezero.plugin.commands.core.CommandManager;
import kiinse.me.zonezero.plugin.commands.zonezero.tabcomplete.*;
import kiinse.me.zonezero.plugin.enums.Config;
import kiinse.me.zonezero.plugin.listeners.*;
import kiinse.me.zonezero.plugin.schedulers.core.SchedulersManager;
import kiinse.me.zonezero.plugin.schedulers.zonezero.PublicKeyScheduler;
import kiinse.me.zonezero.plugin.service.ApiConnection;
import kiinse.me.zonezero.plugin.service.LogFilter;
import kiinse.me.zonezero.plugin.service.ServerAnswer;
import kiinse.me.zonezero.plugin.service.interfaces.ApiService;
import kiinse.me.zonezero.plugin.utils.FilesUtils;
import kiinse.me.zonezero.plugin.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Utility;
import org.bukkit.plugin.java.JavaPlugin;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ZoneZero extends JavaPlugin {

    private final FilesUtils filesUtils = new FilesUtils(this);
    private TomlParseResult configuration = filesUtils.getTomlFile("config.toml");
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
            configuration = filesUtils.getTomlFile("config.toml");
            apiConnection = new ApiConnection(this, toolsTable);
            serverService = new ServerService(apiConnection);
            playersData = new PlayersService(this, apiConnection, settingsTable);
            settingsTable = configuration.getTableOrEmpty(Config.TABLE_SETTINGS.getValue());
            new MessageUtils(filesUtils).reload();
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
            new LogFilter().registerFilter();
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
            pluginManager.registerEvents(new MoveListener(playersData), this);
            pluginManager.registerEvents(new JoinListener(this, playersData, settingsTable, messageUtils), this);
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
                    add("&aRegister your server on &bhttps://t.me/kiinse_bot");
                    add("");
                    add("&aYour server code is '&b" + serverAnswer.getData().getString("message") + "&a'");
                    add("");
                    add("&aEnter this code on &bhttps://t.me/kiinse_bot with command /register <CODE>");
                }});
            } else if (status == 403) {
                sendInFrame(new ArrayList<>(){{
                    add("&cServer core is not allowed!");
                    add("&c" + serverAnswer.getData().getString("message"));
                }});
            } else if (status == 406) {
                runnable.run();
            } else {
                sendInFrame(new ArrayList<>(){{
                    add("&cError on getting server code (Serverside)!");
                }});
            }
        } catch (Exception e) {
            sendLog(Level.SEVERE, onError, e);
            Sentry.captureException(e);
        }
    }

    private void setupSentry() {
        Sentry.init(options -> {
            options.setDsn("https://a95dac05fc644cc1b2bd70f05e0fc5b9@o1138855.ingest.sentry.io/4504871295582208");
            options.setTracesSampleRate(1.0);
            options.setDebug(false);
            options.setRelease(getDescription().getVersion());
        });
    }

    private void loadVariables() {
        isDebug = toolsTable.getBoolean(Config.TOOLS_IS_DEBUG.getValue(), () -> false );
    }

    public static void sendLog(String msg) {
        sendLog(Level.INFO, msg);
    }

    public static void sendLog(Level level, String message, Throwable throwable) {
        sendLog(level, message + " " + throwable.getMessage());
        if (isDebug) throwable.printStackTrace();
    }

    public static void sendLog(Level level, Throwable throwable) {
        sendLog(level, throwable.getMessage());
        if (isDebug) throwable.printStackTrace();
    }

    public static void sendLog(Throwable throwable) {
        sendLog(Level.WARNING, throwable.getMessage());
        if (isDebug) throwable.printStackTrace();
    }

    public static void sendLog(Level level, String msg) {
        if (level.equals(Level.INFO)) {
            sendConsole("&6[&bZoneZero&6]&a " + msg);
        } else if (level.equals(Level.WARNING) || level.equals(Level.SEVERE)) {
            sendConsole("&6[&bZoneZero&f/&c" + level + "&6] " + msg);
        } else {
            if (isDebug) sendConsole("&6[&bZoneZero&f/&dDEBUG&6] " + msg);
        }
    }

    public static void sendInFrame(List<String> list) {
        sendConsole(" &6|==============================");
        for (var it : list) { sendConsole(" &6|  " + it); }
        sendConsole(" &6|==============================");
    }

    @Utility
    public static void sendConsole(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
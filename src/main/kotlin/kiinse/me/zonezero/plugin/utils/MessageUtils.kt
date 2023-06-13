package kiinse.me.zonezero.plugin.utils

import kiinse.me.zonezero.plugin.commands.enums.CommandFailReason
import kiinse.me.zonezero.plugin.config.TomlTable
import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import kiinse.me.zonezero.plugin.config.enums.ConfigTable
import kiinse.me.zonezero.plugin.enums.*
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.tomlj.Toml

class MessageUtils(private val fileUtils: FilesUtils, settings: TomlTable) {

    private val defaultFiles = listOf(Strings.LANG_EN_FILE, Strings.LANG_RU_FILE)
    private val prefix = translateColors(Strings.PLUGIN_PREFIX.value)
    private val messages = HashMap<String, org.tomlj.TomlTable>()
    private val titles = HashMap<String, org.tomlj.TomlTable>()
    private val subtitles = HashMap<String, org.tomlj.TomlTable>()
    private val defaultLocale = settings.get<String>(ConfigKey.DEFAULT_LOCALE) { "en" }
    private val sendTitle = settings.get<Boolean>(ConfigKey.SEND_TITLE) { true }
    val titleTime = (settings.get<Long>(ConfigKey.TITLE_TIME) { 5 }).toInt()

    init {
        loadVariables()
    }

    fun reload() {
        loadVariables()
    }

    private fun loadVariables() {
        messages.clear()
        val directory = fileUtils.getFile("messages")
        fileUtils.createDirectory(directory)
        if (fileUtils.listFilesInDirectory(directory).isEmpty()) {
            defaultFiles.forEach {
                fileUtils.copyFileFromDir(it)
            }
        }
        fileUtils.listFilesInDirectory(directory).forEach {
            messages[it.name.split(".")[0]] = Toml.parse(it.inputStream()).getTableOrEmpty(ConfigTable.MESSAGES.value)
            titles[it.name.split(".")[0]] = Toml.parse(it.inputStream()).getTableOrEmpty(ConfigTable.TITLES.value)
            subtitles[it.name.split(".")[0]] = Toml.parse(it.inputStream()).getTableOrEmpty(ConfigTable.SUBTITLES.value)
        }
    }

    fun sendMessageWithPrefix(sender: CommandSender, message: CommandFailReason) {
        sender.sendMessage("$prefix ${getOrString(sender, message.key)}")
    }

    fun sendMessageWithPrefix(sender: CommandSender, message: Message) {
        sender.sendMessage("$prefix ${getOrString(sender, message.key)}")
    }

    fun sendMessageWithPrefix(player: Player, message: Message) {
        player.sendMessage("$prefix ${getOrString(player, message.key)}")
    }

    fun sendMessage(sender: CommandSender, before: String, message: Message) {
        sender.sendMessage("${translateColors(before)}${getOrString(sender, message.key)}")
    }

    fun sendMessageWithPrefix(sender: CommandSender, message: Message, map: HashMap<String, String>) {
        sender.sendMessage("$prefix ${replace(sender, message, map)}")
    }

    fun sendMessageWithPrefix(player: Player, message: Message, map: HashMap<String, String>) {
        player.sendMessage("$prefix ${replace(player, message, map)}")
    }

    fun sendDisplayMessage(player: Player, title: Title, subtitle: SubTitle, seconds: Int) {
        if (sendTitle) {
            player.sendTitle(getOrStringTitle(player, title), getOrStringSubtitle(player, subtitle), 10, seconds * 20, 20)
        }
    }

    fun sendDisplayMessageWithName(player: Player, title: Title, subtitle: SubTitle, seconds: Int) {
        if (sendTitle) {
            player.sendTitle(getOrStringTitle(player, title),
                             getOrStringSubtitle(player, subtitle).replace(Replace.DISPLAY_NAME.value, player.displayName, ignoreCase = true),
                             10,
                             seconds * 20,
                             20)
        }
    }

    fun replace(string: String, map: HashMap<String, String>): String {
        var msg = string
        map.forEach { (key, value) ->
            msg = msg.replace("<$key>", value, ignoreCase = true)
        }
        return translateColors(msg)
    }

    fun replaceEnums(string: String, map: HashMap<Replace, String>): String {
        var msg = string
        map.forEach { (key, value) -> msg = msg.replace(key.value, value, ignoreCase = true) }
        return translateColors(msg)
    }

    private fun replace(player: Player, message: Message, map: HashMap<String, String>): String {
        var msg = getOrString(player, message.key)
        map.forEach { (key, value) -> msg = msg.replace("<$key>", value, ignoreCase = true) }
        return msg
    }

    private fun replace(commandSender: CommandSender, message: Message, map: HashMap<String, String>): String {
        var msg = getOrString(commandSender, message.key)
        map.forEach { (key, value) -> msg = msg.replace("<$key>", value, ignoreCase = true) }
        return msg
    }

    fun getOrString(player: Player, message: Message): String {
        return getOrString(player, message.key)
    }

    private fun getOrStringTitle(player: Player, title: Title): String {
        return getOrStringTitle(player, title.key)
    }

    private fun getOrStringSubtitle(player: Player, subTitle: SubTitle): String {
        return getOrStringSubtitle(player, subTitle.key)
    }

    private fun translateColors(str: String): String {
        return ChatColor.translateAlternateColorCodes('&', str)
    }

    private fun getOrString(player: Player, key: String): String {
        val messages = getLocaleMessagesTable(player) ?: return key
        return translateColors(messages.getString(key) { key })
    }

    private fun getOrStringTitle(player: Player, key: String): String {
        val messages = getLocaleTitleTable(player) ?: return key
        return translateColors(messages.getString(key) { key })
    }

    private fun getOrStringSubtitle(player: Player, key: String): String {
        val messages = getLocaleSubtitleTable(player) ?: return key
        return translateColors(messages.getString(key) { key })
    }

    private fun getOrString(commandSender: CommandSender, key: String): String {
        val messages = getLocaleMessagesTable(commandSender) ?: return key
        return translateColors(messages.getString(key) { key })
    }

    private fun getLocaleMessagesTable(commandSender: CommandSender): org.tomlj.TomlTable? {
        if (commandSender is ConsoleCommandSender) return messages[defaultLocale]
        val player = commandSender as Player
        val playerLocale = player.locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return if (messages.contains(playerLocale)) messages[playerLocale] else messages[defaultLocale]
    }

    private fun getLocaleMessagesTable(player: Player): org.tomlj.TomlTable? {
        val playerLocale = player.locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return if (messages.contains(playerLocale)) messages[playerLocale] else messages[defaultLocale]
    }

    private fun getLocaleTitleTable(player: Player): org.tomlj.TomlTable? {
        val playerLocale = player.locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return if (titles.contains(playerLocale)) titles[playerLocale] else titles[defaultLocale]
    }

    private fun getLocaleSubtitleTable(player: Player): org.tomlj.TomlTable? {
        val playerLocale = player.locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return if (subtitles.contains(playerLocale)) subtitles[playerLocale] else subtitles[defaultLocale]
    }
}
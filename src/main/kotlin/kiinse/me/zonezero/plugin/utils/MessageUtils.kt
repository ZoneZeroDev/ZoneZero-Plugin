package kiinse.me.zonezero.plugin.utils

import kiinse.me.zonezero.plugin.commands.enums.CommandFailReason
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.enums.Replace
import kiinse.me.zonezero.plugin.enums.Strings
import org.apache.commons.io.FileUtils
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.tomlj.Toml
import org.tomlj.TomlTable

class MessageUtils(private val fileUtils: FilesUtils, settings: TomlTable) {

    private val prefix = translateColors(Strings.PLUGIN_PREFIX.value)
    private val messages = HashMap<String, TomlTable>()
    private val defaultLocale = settings.getString(Config.DEFAULT_LOCALE.value) { "en" }

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
            fileUtils.getFilesInDirectoryInJar("messages").forEach {
                FileUtils.copyInputStreamToFile(fileUtils.accessFile("messages/$it"), fileUtils.getFile("messages", it))
            }
        }
        fileUtils.listFilesInDirectory(directory).forEach {
            messages[it.name.split(".")[0]] = Toml.parse(it.inputStream()).getTableOrEmpty(Config.TABLE_MESSAGES.value)
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

    private fun translateColors(str: String): String {
        return ChatColor.translateAlternateColorCodes('&', str)
    }

    private fun getOrString(player: Player, key: String): String {
        val messages = getLocaleTable(player) ?: return key
        return translateColors(messages.getString(key) { key })
    }
    private fun getOrString(commandSender: CommandSender, key: String): String {
        val messages = getLocaleTable(commandSender) ?: return key
        return translateColors(messages.getString(key) { key })
    }

    private fun getLocaleTable(commandSender: CommandSender): TomlTable? {
        if (commandSender is ConsoleCommandSender) return messages[defaultLocale]
        val player = commandSender as Player
        val playerLocale = player.locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return if (messages.contains(playerLocale)) messages[playerLocale] else messages[defaultLocale]
    }

    private fun getLocaleTable(player: Player): TomlTable? {
        val playerLocale = player.locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        return if (messages.contains(playerLocale)) messages[playerLocale] else messages[defaultLocale]
    }
}
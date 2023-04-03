package kiinse.me.zonezero.plugin.utils

import kiinse.me.zonezero.plugin.commands.enums.CommandFailReason
import kiinse.me.zonezero.plugin.enums.Message
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.tomlj.TomlTable

class MessageUtils(private val fileUtils: FilesUtils) {

    private val prefix = translateColors("&6&l[&bZoneZero&6&l]&f:")
    private var messages: TomlTable

    init {
        messages = fileUtils.getTomlFile("messages.toml").getTableOrEmpty("messages")
    }

    fun reload() {
        messages = fileUtils.getTomlFile("messages.toml").getTableOrEmpty("messages")
    }

    fun sendMessageWithPrefix(sender: CommandSender, message: CommandFailReason) {
        sender.sendMessage("$prefix ${getOrString(message.key)}")
    }

    fun sendMessageWithPrefix(sender: CommandSender, message: Message) {
        sender.sendMessage("$prefix ${getOrString(message.key)}")
    }

    fun sendMessageWithPrefix(player: Player, message: Message) {
        player.sendMessage("$prefix ${getOrString(message.key)}")
    }

    fun sendMessage(sender: CommandSender, before: String, message: Message) {
        sender.sendMessage("${translateColors(before)}${getOrString(message.key)}")
    }

    fun sendMessage(sender: CommandSender, message: Message) {
        sender.sendMessage(getOrString(message.key))
    }

    fun sendMessage(player: Player, message: Message) {
        player.sendMessage(getOrString(message.key))
    }

    fun sendMessageWithPrefix(sender: CommandSender, message: Message, map: HashMap<String, String>) {
        sender.sendMessage("$prefix ${replace(message, map)}")
    }

    fun sendMessageWithPrefix(player: Player, message: Message, map: HashMap<String, String>) {
        player.sendMessage("$prefix ${replace(message, map)}")
    }

    fun sendMessage(sender: CommandSender, message: Message, map: HashMap<String, String>) {
        sender.sendMessage(replace(message, map))
    }

    fun sendMessage(player: Player, message: Message, map: HashMap<String, String>) {
        player.sendMessage(replace(message, map))
    }

    fun replace(string: String, map: HashMap<String, String>): String {
        var msg = string
        map.forEach { (key, value) ->
            msg = msg.replace("<$key>", value, ignoreCase = true)
        }
        return translateColors(msg)
    }

    private fun replace(message: Message, map: HashMap<String, String>): String {
        var msg = getOrString(message.key)
        map.forEach { (key, value) ->
            msg = msg.replace("<$key>", value, ignoreCase = true)
        }
        return msg
    }

    fun getOrString(message: Message): String {
        return getOrString(message.key)
    }

    fun translateColors(str: String): String {
        return ChatColor.translateAlternateColorCodes('&', str)
    }

    private fun getOrString(key: String): String {
        return translateColors(messages.getString(key) { key })
    }

}
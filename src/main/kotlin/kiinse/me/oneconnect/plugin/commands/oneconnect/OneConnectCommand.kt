package kiinse.me.oneconnect.plugin.commands.oneconnect

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.commands.abstracts.MineCommand
import kiinse.me.oneconnect.plugin.commands.annotations.Command
import kiinse.me.oneconnect.plugin.commands.annotations.SubCommand
import kiinse.me.oneconnect.plugin.commands.interfaces.MineCommandContext
import kiinse.me.oneconnect.plugin.enums.Message
import kiinse.me.oneconnect.plugin.utils.MessageUtils
import org.bukkit.command.CommandSender

class OneConnectCommand(plugin: OneConnect) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "oneconnect", permission = "oneconnect.player.help")
    fun oneconnect(context: MineCommandContext) {
        sendHelp(context.sender)
    }

    @SubCommand(command = "help", permission = "oneconnect.player.help")
    fun help(context: MineCommandContext) {
        sendHelp(context.sender)
    }

    @SubCommand(command = "reload", permission = "oneconnect.admin.reload")
    fun reload(context: MineCommandContext) {
        if (context.args[0].equals("reload", ignoreCase = true)) {
            try {
                plugin.onReload()
                messageUtils.sendMessageWithPrefix(context.sender, Message.PLUGIN_RESTARTED)
            } catch (e: Exception) {
                messageUtils.sendMessageWithPrefix(context.sender, Message.ERROR_ON_RESTART, hashMapOf(Pair("error", e.message ?: "Null")))
            }
        }
    }

    private fun sendHelp(sender: CommandSender) {
        messageUtils.sendMessage(sender, "&d=========== &6&l[&bOneConnect&6&l] &d===========\n", Message.HELP_MESSAGE)
    }
}
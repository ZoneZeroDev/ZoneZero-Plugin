package kiinse.me.zonezero.plugin.commands.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.commands.abstracts.MineCommand
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.annotations.SubCommand
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.command.CommandSender

@Suppress("unused")
class ZoneZeroCommand(plugin: ZoneZero) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "zonezero", permission = "zonezero.player.help")
    fun zonezero(context: MineCommandContext) {
        sendHelp(context.sender)
    }

    @SubCommand(command = "help", permission = "zonezero.player.help")
    fun help(context: MineCommandContext) {
        sendHelp(context.sender)
    }

    @SubCommand(command = "reload", permission = "zonezero.admin.reload")
    fun reload(context: MineCommandContext) {
        try {
            plugin.onReload()
            messageUtils.sendMessageWithPrefix(context.sender, Message.PLUGIN_RESTARTED)
        } catch (e: Exception) {
            messageUtils.sendMessageWithPrefix(context.sender, Message.ERROR_ON_RESTART, hashMapOf(Pair("error", e.message ?: "Null")))
        }
    }

    private fun sendHelp(sender: CommandSender) {
        messageUtils.sendMessage(sender, Strings.ZONE_ZERO_TOP.value, Message.HELP_MESSAGE)
    }
}
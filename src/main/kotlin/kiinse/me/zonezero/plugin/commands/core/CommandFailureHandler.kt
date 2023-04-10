package kiinse.me.zonezero.plugin.commands.core

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.commands.enums.CommandFailReason
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandFailureHandler
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.command.CommandSender

class CommandFailureHandler(plugin: ZoneZero) : MineCommandFailureHandler {

    private val messagesUtils: MessageUtils = plugin.messageUtils

    override fun handleFailure(reason: CommandFailReason, sender: CommandSender) {
        messagesUtils.sendMessageWithPrefix(sender, reason)
    }
}
package kiinse.me.oneconnect.plugin.commands.core

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.commands.enums.CommandFailReason
import kiinse.me.oneconnect.plugin.commands.interfaces.MineCommandFailureHandler
import kiinse.me.oneconnect.plugin.commands.abstracts.RegisteredCommand
import kiinse.me.oneconnect.plugin.utils.MessageUtils
import org.bukkit.command.CommandSender

class CommandFailureHandler(plugin: OneConnect) : MineCommandFailureHandler {

    private val messagesUtils: MessageUtils = plugin.messageUtils

    override fun handleFailure(reason: CommandFailReason, sender: CommandSender, command: RegisteredCommand?) {
        messagesUtils.sendMessageWithPrefix(sender, reason)
    }
}
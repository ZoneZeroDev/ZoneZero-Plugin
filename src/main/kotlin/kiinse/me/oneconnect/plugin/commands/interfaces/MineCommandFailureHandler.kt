package kiinse.me.oneconnect.plugin.commands.interfaces

import kiinse.me.oneconnect.plugin.commands.abstracts.RegisteredCommand
import kiinse.me.oneconnect.plugin.commands.enums.CommandFailReason
import org.bukkit.command.CommandSender

interface MineCommandFailureHandler {
    fun handleFailure(reason: CommandFailReason, sender: CommandSender, command: RegisteredCommand?)
}
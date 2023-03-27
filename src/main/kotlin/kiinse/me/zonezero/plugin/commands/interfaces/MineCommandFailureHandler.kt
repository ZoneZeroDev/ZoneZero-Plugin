package kiinse.me.zonezero.plugin.commands.interfaces

import kiinse.me.zonezero.plugin.commands.abstracts.RegisteredCommand
import kiinse.me.zonezero.plugin.commands.enums.CommandFailReason
import org.bukkit.command.CommandSender

interface MineCommandFailureHandler {
    fun handleFailure(reason: CommandFailReason, sender: CommandSender, command: RegisteredCommand?)
}
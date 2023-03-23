package kiinse.me.oneconnect.plugin.commands.interfaces

import org.bukkit.command.CommandSender

@Suppress("UNUSED")
interface MineCommandContext {
    val sender: CommandSender
    val args: Array<String>
}
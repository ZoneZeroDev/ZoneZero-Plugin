package kiinse.me.zonezero.plugin.commands.core

import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import org.bukkit.command.CommandSender

class CommandContext(override val sender: CommandSender, override val args: Array<String>) : MineCommandContext {

    override fun equals(other: Any?): Boolean {
        return other != null && other is MineCommandContext && other.hashCode() == hashCode()
    }

    override fun toString(): String {
        return """
            Sender: ${sender.name}
            Args: ${args.contentToString()}
            """.trimIndent()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }
}
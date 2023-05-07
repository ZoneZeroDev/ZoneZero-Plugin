package kiinse.me.zonezero.plugin.commands.zonezero.tabcomplete

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RemoveAccountTab : TabCompleter {

    override fun onTabComplete(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): List<String> {
        val list = ArrayList<String>()
        if (sender is Player && cmd.name.equals("login", ignoreCase = true)) {
            if (args.size == 1 && sender.hasPermission("zonezero.player.login")) {
                list.add("<PASSWORD>")
            }
            list.sort()
        }
        return list
    }
}

package kiinse.me.oneconnect.plugin.commands.oneconnect.tabcomplete

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RegisterTab : TabCompleter {

    override fun onTabComplete(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): List<String> {
        val list = ArrayList<String>()
        if (sender is Player && cmd.name.equals("register", ignoreCase = true)) {
            if (sender.hasPermission("oneconnect.player.register")) {
                if (args.size == 1) {
                    list.add("<PASSWORD>")
                } else if (args.size == 2) {
                    list.add("<PASSWORD>")
                }
            }
            list.sort()
        }
        return list
    }
}
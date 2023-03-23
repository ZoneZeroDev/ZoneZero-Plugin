package kiinse.me.oneconnect.plugin.commands.oneconnect.tabcomplete

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class TwoFactorTab : TabCompleter {

    override fun onTabComplete(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): List<String> {
        val list = ArrayList<String>()
        if (sender is Player && cmd.name.equals("2fa", ignoreCase = true)) {
            if (sender.hasPermission("oneconnect.player.2fa")) {
                if (args.size == 1) {
                    list.add("<CODE>")
                    list.add("help")
                    list.add("enable")
                    list.add("disable")
                } else if (args.size == 2) {
                    if (args[0].equals("enable", ignoreCase = true) || args[0].equals("disable", ignoreCase = true)) {
                        list.add("<PASSWORD>")
                    }
                } else if (args.size == 3) {
                    if (args[0].equals("enable", ignoreCase = true)) {
                        list.add("<EMAIL>")
                    }
                } else if (args.size == 4) {
                    if (args[0].equals("enable", ignoreCase = true)) {
                        list.add("<EMAIL>")
                    }
                }
            }
            list.sort()
        }
        return list
    }
}

package kiinse.me.oneconnect.plugin.commands.oneconnect.tabcomplete

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class LoginTab : TabCompleter {

    override fun onTabComplete(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): List<String> {
        val list = ArrayList<String>()
        if (sender is Player && cmd.name.equals("login", ignoreCase = true)) {
            if (args.size == 1 && sender.hasPermission("oneconnect.player.login")) {
                list.add("<PASSWORD>")
            }
            list.sort()
        }
        return list
    }
}

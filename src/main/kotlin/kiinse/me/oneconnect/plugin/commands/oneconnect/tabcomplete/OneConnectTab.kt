package kiinse.me.oneconnect.plugin.commands.oneconnect.tabcomplete

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class OneConnectTab : TabCompleter {

    override fun onTabComplete(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): List<String> {
        val list = ArrayList<String>()
        if (sender is Player && cmd.name.equals("oneconnect", ignoreCase = true)) {
            if (args.size == 1 && sender.hasPermission("oneconnect.player.help")) {
                list.add("help")
            }
            if (args.size == 1 && sender.hasPermission("oneconnect.admin.reload")) {
                list.add("reload")
            }
            list.sort()
        }
        return list
    }
}

package kiinse.me.zonezero.plugin.commands.zonezero.tabcomplete

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ChangePasswordTab : TabCompleter {

    override fun onTabComplete(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): List<String> {
        val list = ArrayList<String>()
        if (sender is Player && cmd.name.equals("changepassword", ignoreCase = true)) {
            if (sender.hasPermission("zonezero.player.changepassword")) {
                when (args.size) {
                    1 -> list.add("<OLD_PASSWORD>")
                    2 -> list.add("<NEW_PASSWORD>")
                    3 -> list.add("<NEW_PASSWORD>")
                }
            }
            list.sort()
        }
        return list
    }
}

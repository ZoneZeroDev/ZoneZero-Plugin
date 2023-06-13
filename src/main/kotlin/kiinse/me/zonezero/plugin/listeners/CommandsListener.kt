package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.config.TomlTable
import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class CommandsListener(private val playersData: PlayersData, private val messageUtils: MessageUtils, config: TomlTable) : Listener {

    private val allowedCommands = config.getArray(ConfigKey.SETTINGS_ALLOW_COMMANDS)

    @EventHandler
    fun onPlayerChat(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (playersData.getPlayerStatus(player) != PlayerStatus.AUTHORIZED && !canUseCommand(event)) {
            messageUtils.sendMessageWithPrefix(player, Message.AUTHORIZE_ON_SERVER)
            event.isCancelled = true
        }
    }

    private fun canUseCommand(event: PlayerCommandPreprocessEvent): Boolean {
        allowedCommands.toList().forEach {
            if (event.message.startsWith(it.toString(), ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}

package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.tomlj.TomlTable

class QuitListener(private val playersData: PlayersData,
                   config: TomlTable,
                   private val messageUtils: MessageUtils) : Listener {

    private val removeLeaveMessage = config.getBoolean(Config.REMOVE_LEAVE_MESSAGE.value) { false }
    private val removeUnloggedLeaveMessage = config.getBoolean(Config.REMOVE_UNLOGGED_LEAVE_MESSAGE.value) { false }
    private val customLeaveMessage = config.getString(Config.CUSTOM_LEAVE_MESSAGE.value) { "" }

    @EventHandler
    fun onPlayerJoin(event: PlayerQuitEvent) {
        onThisEvent(event)
    }

    private fun onThisEvent(event: PlayerQuitEvent) {
        val player = event.player
        if (removeLeaveMessage) {
            event.quitMessage = ""
        } else {
            if (playersData.getPlayerStatus(player) == PlayerStatus.NOT_AUTHORIZED && removeUnloggedLeaveMessage) {
                event.quitMessage = ""
            }
            if (customLeaveMessage.isNotEmpty()) {
                event.quitMessage = messageUtils.replace(customLeaveMessage, hashMapOf(Pair("player_name", player.name),
                                                                                       Pair("display_name", player.displayName),
                                                                                       Pair("display_name_no_color", "&f${player.displayName}")))
            }
        }
    }
}
package kiinse.me.oneconnect.plugin.listeners

import kiinse.me.oneconnect.plugin.apiserver.interfaces.PlayersData
import kiinse.me.oneconnect.plugin.apiserver.enums.PlayerStatus
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MoveListener(private val playersData: PlayersData) : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!isPlayerAllowToMove(event.player)) {
            event.isCancelled = true
        }
    }

    private fun isPlayerAllowToMove(player: Player): Boolean {
        return playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED
    }
}
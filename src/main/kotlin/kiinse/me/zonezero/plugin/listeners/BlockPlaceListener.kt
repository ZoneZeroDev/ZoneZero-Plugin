package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockPlaceListener(private val playersData: PlayersData) : Listener {

    @EventHandler
    fun onPlayerBlockPlace(event: BlockPlaceEvent) {
        if (!isPlayerAllowPlace(event.player)) {
            event.isCancelled = true
        }
    }

    private fun isPlayerAllowPlace(player: Player): Boolean {
        return playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED
    }
}
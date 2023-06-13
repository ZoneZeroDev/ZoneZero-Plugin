package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class InteractListener(private val playersData: PlayersData) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!isPlayerAllowInteract(event.player)) {
            event.isCancelled = true
        }
    }

    private fun isPlayerAllowInteract(player: Player): Boolean {
        return playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED
    }
}
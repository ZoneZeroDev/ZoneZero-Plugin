package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class InventoryListener(private val playersData: PlayersData) : Listener {

    @EventHandler
    fun onPlayerInventoryOpen(event: InventoryOpenEvent) {
        val player = event.player
        if (player is Player && !isPlayerCanUse(player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked
        if (player is Player && !isPlayerCanUse(player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked
        if (player is Player && !isPlayerCanUse(player)) {
            event.isCancelled = true
        }
    }

    private fun isPlayerCanUse(player: Player): Boolean {
        return playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED
    }
}
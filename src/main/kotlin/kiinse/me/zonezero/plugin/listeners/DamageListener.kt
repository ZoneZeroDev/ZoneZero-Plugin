package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class DamageListener(private val playersData: PlayersData) : Listener {

    @EventHandler
    fun onPlayerInteract(event: EntityDamageEvent) {
        val player = event.entity
        if (player is Player && !isPlayerAllowDamage(player)) {
            event.isCancelled = true
        }
    }

    private fun isPlayerAllowDamage(player: Player): Boolean {
        return playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED
    }
}
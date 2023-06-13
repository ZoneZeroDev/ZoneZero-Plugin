package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class ExitListener(private val playersData: PlayersData) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerQuitEvent) {
        onThisEvent(event)
    }

    private fun onThisEvent(event: PlayerQuitEvent) {
        val player = event.player
        playersData.setPlayerStatus(event.player, PlayerStatus.NOT_AUTHORIZED)
        playersData.removeJoinMessage(player)
    }
}
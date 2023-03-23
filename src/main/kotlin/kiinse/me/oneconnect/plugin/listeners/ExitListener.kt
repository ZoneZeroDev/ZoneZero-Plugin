package kiinse.me.oneconnect.plugin.listeners

import kiinse.me.oneconnect.plugin.apiserver.interfaces.PlayersData
import kiinse.me.oneconnect.plugin.apiserver.enums.PlayerStatus
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class ExitListener(private val playersData: PlayersData) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerQuitEvent) {
        onThisEvent(event)
    }

    private fun onThisEvent(event: PlayerQuitEvent) {
        playersData.setPlayerStatus(event.player, PlayerStatus.NOT_AUTHORIZED)
    }
}
package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.config.TomlTable
import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener(private val playersData: PlayersData, config: TomlTable) : Listener {

    private val allowChat = config.get<Boolean>(ConfigKey.SETTINGS_ALLOW_CHAT) { false }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (playersData.getPlayerStatus(event.player) != PlayerStatus.AUTHORIZED && !allowChat) {
            event.isCancelled = true
        }
    }
}

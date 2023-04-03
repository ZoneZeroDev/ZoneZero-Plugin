package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.enums.Config
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.tomlj.TomlTable

class ChatListener(private val playersData: PlayersData, config: TomlTable): Listener {

    private val allowChat = config.getBoolean(Config.SETTINGS_ALLOW_CHAT.value) { false }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (playersData.getPlayerStatus(event.player) != PlayerStatus.AUTHORIZED && !allowChat) {
            event.isCancelled = true
        }
    }
}

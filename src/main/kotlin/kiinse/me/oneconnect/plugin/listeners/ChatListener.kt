package kiinse.me.oneconnect.plugin.listeners

import kiinse.me.oneconnect.plugin.apiserver.enums.PlayerStatus
import kiinse.me.oneconnect.plugin.apiserver.interfaces.PlayersData
import kiinse.me.oneconnect.plugin.enums.Config
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.tomlj.TomlTable

class ChatListener(private val playersData: PlayersData, private val config: TomlTable): Listener {

    private val allowChat = config.getBoolean(Config.SETTINGS_ALLOW_CHAT.value) { false }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (playersData.getPlayerStatus(event.player) != PlayerStatus.AUTHORIZED && !allowChat) {
            event.isCancelled = true
        }
    }
}

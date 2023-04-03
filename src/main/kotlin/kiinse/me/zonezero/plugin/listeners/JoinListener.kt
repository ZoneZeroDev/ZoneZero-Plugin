package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.tomlj.TomlTable

class JoinListener(private val playersData: PlayersData,
                   config: TomlTable,
                   private val messageUtils: MessageUtils) : Listener {

    private val removeJoinMessage = config.getBoolean(Config.REMOVE_JOIN_MESSAGE.value) { false }
    private val customJoinMessage = config.getString(Config.CUSTOM_JOIN_MESSAGE.value) { "" }
    private val kickNonRegistered = config.getBoolean(Config.KICK_UNREGISTERED.value) { false }
    private val joinMessageOnAuth = config.getBoolean(Config.JOIN_MESSAGE_ON_AUTH.value) { false }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        onThisEvent(event)
    }

    private fun onThisEvent(event: PlayerJoinEvent) {
        val player = event.player
        playersData.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
        if (!player.hasPlayedBefore()) { messageUtils.sendMessageWithPrefix(player, Message.WELCOME_MESSAGE) }
        if (removeJoinMessage) {
            event.joinMessage = ""
        } else {
            val joinMessage = getJoinMessage(player, event)
            if (joinMessageOnAuth) {
                playersData.addJoinMessage(player, joinMessage)
                event.joinMessage = ""
            } else {
                event.joinMessage = joinMessage
            }
        }
        playersData.authPlayerByIp(player) { answer ->
            run {
                when (answer.status) {
                    200 -> {
                        playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
                        messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_LOGGED_IN_IP)
                    }
                    202 -> messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_SENT)
                    429 -> messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_LOGIN)
                    403 -> messageUtils.sendMessageWithPrefix(player, Message.PLEASE_LOGIN)
                    404 -> {
                        if (kickNonRegistered) {
                            player.kickPlayer(messageUtils.getOrString(Message.KICK_UNREGISTERED))
                        } else {
                            messageUtils.sendMessageWithPrefix(player, Message.PLEASE_REGISTER)
                        }
                    }
                }
            }
        }.start()
    }

    private fun getJoinMessage(player: Player, event: PlayerJoinEvent): String {
        if (customJoinMessage.isNotEmpty()) {
            return messageUtils.replace(customJoinMessage, hashMapOf(Pair("player_name", player.name),
                Pair("display_name", player.displayName),
                Pair("display_name_no_color", "&f${player.displayName}")))
        }
        return event.joinMessage ?: ""
    }
}
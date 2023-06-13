package kiinse.me.zonezero.plugin.listeners

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.config.TomlTable
import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import kiinse.me.zonezero.plugin.enums.*
import kiinse.me.zonezero.plugin.messages.MessageBuilder
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.concurrent.TimeUnit

class JoinListener(private val zoneZero: ZoneZero,
                   private val playersData: PlayersData,
                   config: TomlTable,
                   private val messageUtils: MessageUtils) : Listener {

    private val removeJoinMessage = config.get<Boolean>(ConfigKey.REMOVE_JOIN_MESSAGE) { false }
    private val customJoinMessage = config.get<String>(ConfigKey.CUSTOM_JOIN_MESSAGE) { "" }
    private val kickNonRegistered = config.get<Boolean>(ConfigKey.KICK_UNREGISTERED) { false }
    private val joinMessageOnAuth = config.get<Boolean>(ConfigKey.JOIN_MESSAGE_ON_AUTH) { false }
    private val joinDelay = config.get<Long>(ConfigKey.JOIN_DELAY) { 70 }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.resetTitle()
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
        Bukkit.getScheduler().runTaskLater(zoneZero, Runnable { onThisEvent(player) }, joinDelay)
    }

    private fun onThisEvent(player: Player) {
        playersData.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
        if (!player.hasPlayedBefore()) {
            messageUtils.sendMessageWithPrefix(player, Message.WELCOME_MESSAGE)
        }
        playersData.authPlayerByIp(player) { answer ->
            run {
                when (answer.status) {
                    200 -> {
                        playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.SUCCESSFULLY_LOGGED_IN_IP)
                            .setTitle(Title.WELCOME)
                            .setSubTitle(SubTitle.WELCOME)
                            .setTitleType(TitleType.DISPLAY_NAME)
                            .send()
                    }

                    202 -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TWO_FACTOR_SENT)
                            .setTitle(Title.LOGIN)
                            .setSubTitle(SubTitle.TWO_FA_SEND)
                            .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                            .send()
                    }

                    429 -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.ERROR_ON_LOGIN)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }

                    403 -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.PLEASE_LOGIN)
                            .setTitle(Title.LOGIN)
                            .setSubTitle(SubTitle.LOGIN)
                            .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                            .send()
                    }

                    404 -> {
                        if (kickNonRegistered) {
                            player.kickPlayer(messageUtils.getOrString(player, Message.KICK_UNREGISTERED))
                        } else {
                            MessageBuilder(messageUtils, player)
                                .setMessage(Message.PLEASE_REGISTER)
                                .setTitle(Title.REGISTER)
                                .setSubTitle(SubTitle.REGISTER)
                                .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                                .send()
                        }
                    }
                }
            }
        }.start()
    }

    private fun getJoinMessage(player: Player, event: PlayerJoinEvent): String {
        if (customJoinMessage.isNotEmpty()) {
            return messageUtils.replaceEnums(customJoinMessage, hashMapOf(Pair(Replace.PLAYER_NAME, player.name),
                                                                          Pair(Replace.DISPLAY_NAME, player.displayName),
                                                                          Pair(Replace.DISPLAY_NAME_NO_COLOR, "&f${player.displayName}")))
        }
        return event.joinMessage ?: ""
    }
}
package kiinse.me.zonezero.plugin.commands.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.commands.abstracts.MineCommand
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import kiinse.me.zonezero.plugin.config.TomlTable
import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.enums.SubTitle
import kiinse.me.zonezero.plugin.enums.Title
import kiinse.me.zonezero.plugin.enums.TitleType
import kiinse.me.zonezero.plugin.messages.MessageBuilder
import kiinse.me.zonezero.plugin.service.body.PlayerLoginBody
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

@Suppress("unused")
class LoginCommand(plugin: ZoneZero, private val playersData: PlayersData, config: TomlTable) : MineCommand(plugin) {

    private val kickOnWrongPassword = config.get<Boolean>(ConfigKey.KICK_WRONG_PASSWORD) { false }
    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "login", permission = "zonezero.player.login", disallowNonPlayer = true, parameters = 1)
    fun login(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        if (playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.ALREADY_LOGGED_IN)
            return
        }
        playersData.authPlayer(player, PlayerLoginBody(context.args[0], playersData.getPlayerIp(player))) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.SUCCESSFULLY_LOGGED_IN)
                            .setTitle(Title.WELCOME)
                            .setSubTitle(SubTitle.WELCOME)
                            .setTitleType(TitleType.DISPLAY_NAME)
                            .send()
                    }

                    202  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TWO_FACTOR_SENT)
                            .setTitle(Title.TWO_FA_SEND)
                            .setSubTitle(SubTitle.TWO_FA_SEND)
                            .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                            .send()
                    }

                    401  -> {
                        if (kickOnWrongPassword) {
                            player.kickPlayer(messageUtils.getOrString(player, Message.WRONG_PASSWORD))
                        } else {
                            MessageBuilder(messageUtils, player)
                                .setMessage(Message.WRONG_PASSWORD)
                                .setTitle(Title.PASSWORD_WRONG)
                                .setSubTitle(SubTitle.PASSWORD_WRONG)
                                .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                                .send()
                        }
                    }

                    404  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.NOT_REGISTERED)
                            .setTitle(Title.REGISTER)
                            .setSubTitle(SubTitle.REGISTER)
                            .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                            .send()
                    }

                    429  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TOO_MANY_ATTEMPTS)
                            .setReplaceMap(hashMapOf(Pair("seconds", answer.getMessage().split("'")[1])))
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }

                    else -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.ERROR_ON_LOGIN)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }
                }
            }
        }.start()
    }
}
package kiinse.me.zonezero.plugin.commands.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.commands.abstracts.MineCommand
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.enums.SubTitle
import kiinse.me.zonezero.plugin.enums.Title
import kiinse.me.zonezero.plugin.messages.MessageBuilder
import kiinse.me.zonezero.plugin.service.body.PlayerRegisterBody
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class RegisterCommand(plugin: ZoneZero, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "register", permission = "zonezero.player.register", disallowNonPlayer = true, parameters = 2)
    fun register(context: MineCommandContext) {
        val player = context.sender as Player
        if (playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.ALREADY_LOGGED_IN)
            return
        }
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        val args = context.args
        val password = args[0]
        if (password != args[1]) {
            MessageBuilder(messageUtils, player)
                .setMessage(Message.PASSWORD_MISMATCH)
                .setTitle(Title.PASSWORD_MISMATCH)
                .setSubTitle(SubTitle.PASSWORD_MISMATCH)
                .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                .send()
            return
        }
        playersData.registerPlayer(player, PlayerRegisterBody(password, "", playersData.getPlayerIp(player))) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.SUCCESSFULLY_REGISTERED)
                            .setTitle(Title.REGISTER_SUCCESS)
                            .setSubTitle(SubTitle.REGISTER_SUCCESS)
                            .send()
                    }

                    406  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.WRONG_PASSWORD_SIZE)
                            .setReplaceMap(hashMapOf(Pair("size", answer.getMessage().split("than ")[1])))
                            .setTitle(Title.PASSWORD_UNSAFE)
                            .setSubTitle(SubTitle.PASSWORD_UNSAFE)
                            .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                            .send()
                    }

                    403  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.ALREADY_REGISTERED)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
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
                            .setMessage(Message.ERROR_ON_REGISTER)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }
                }
            }
        }.start()
    }
}
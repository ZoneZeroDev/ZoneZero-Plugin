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
import kiinse.me.zonezero.plugin.service.body.PlayerPasswordChangeBody
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

@Suppress("unused")
class ChangePasswordCommand(plugin: ZoneZero, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "changepassword", permission = "zonezero.player.changepassword", disallowNonPlayer = true, parameters = 3)
    fun changePassword(context: MineCommandContext) {
        val player = context.sender as Player
        if (playersData.getPlayerStatus(player) != PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.AUTHORIZE_ON_SERVER)
            return
        }
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        val args = context.args
        val password = args[1]
        if (password != args[2]) {
            MessageBuilder(messageUtils, player)
                .setMessage(Message.PASSWORD_MISMATCH)
                .setTitle(Title.PASSWORD_MISMATCH)
                .setSubTitle(SubTitle.PASSWORD_MISMATCH)
                .send()
            return
        }
        playersData.changePassword(player, PlayerPasswordChangeBody(args[0], password)) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.SUCCESSFULLY_PASSWORD_CHANGED)
                            .setTitle(Title.PASSWORD_CHANGED)
                            .setSubTitle(SubTitle.PASSWORD_CHANGED)
                            .send()
                    }

                    202  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TWO_FACTOR_SENT)
                            .setTitle(Title.TWO_FA_SEND)
                            .setSubTitle(SubTitle.TWO_FA_SEND)
                            .send()
                    }

                    401  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.WRONG_PASSWORD)
                            .setTitle(Title.PASSWORD_WRONG)
                            .setSubTitle(SubTitle.PASSWORD_WRONG)
                            .send()
                    }

                    404  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.NOT_REGISTERED)
                            .setTitle(Title.REGISTER)
                            .setSubTitle(SubTitle.REGISTER)
                            .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                            .send()
                    }

                    406  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.WRONG_PASSWORD_SIZE)
                            .setReplaceMap(hashMapOf(Pair("size", answer.getMessage().split("than ")[1])))
                            .setTitle(Title.PASSWORD_UNSAFE)
                            .setSubTitle(SubTitle.PASSWORD_UNSAFE)
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
                            .setMessage(Message.ERROR_ON_PASSWORD_CHANGE)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }
                }
            }
        }.start()
    }
}
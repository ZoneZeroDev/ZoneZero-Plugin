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
import kiinse.me.zonezero.plugin.service.body.PlayerRemoveBody
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

@Suppress("unused")
class RemoveAccountCommand(plugin: ZoneZero, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "zzremove", permission = "zonezero.player.remove", disallowNonPlayer = true, parameters = 1)
    fun remove(context: MineCommandContext) {
        val player = context.sender as Player
        if (playersData.getPlayerStatus(player) == PlayerStatus.NOT_AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.PLEASE_LOGIN)
            return
        }
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        playersData.removePlayer(player, PlayerRemoveBody(context.args[0])) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        playersData.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.SUCCESSFULLY_ACCOUNT_REMOVED)
                            .setTitle(Title.REGISTER)
                            .setSubTitle(SubTitle.REGISTER)
                            .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                            .send()
                    }

                    202  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TWO_FACTOR_SENT)
                            .setTitle(Title.TWO_FA_SEND)
                            .setSubTitle(SubTitle.TWO_FA_SEND)
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
                            .setMessage(Message.ERROR_ON_REMOVE)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }
                }
            }
        }.start()
    }
}
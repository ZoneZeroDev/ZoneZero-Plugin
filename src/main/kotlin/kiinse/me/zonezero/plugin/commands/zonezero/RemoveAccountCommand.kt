package kiinse.me.zonezero.plugin.commands.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.commands.abstracts.MineCommand
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.service.body.PlayerRemoveBody
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player

@Suppress("unused")
class RemoveAccountCommand(plugin: ZoneZero, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "zzremove", permission = "zonezero.player.remove", disallowNonPlayer = true, parameters = 1)
    fun remove(context: MineCommandContext) {
        val player = context.sender as Player
        if (playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.ALREADY_LOGGED_IN)
            return
        }
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        playersData.removePlayer(player, PlayerRemoveBody(context.args[0])) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        playersData.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
                        messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_ACCOUNT_REMOVED)
                        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_REGISTER)
                    }
                    202  -> messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_SENT)
                    404  -> messageUtils.sendMessageWithPrefix(player, Message.NOT_REGISTERED)
                    429  -> messageUtils.sendMessageWithPrefix(player, Message.TOO_MANY_ATTEMPTS,
                                                               hashMapOf(Pair("seconds", answer.getMessageAnswer().message.split("'")[1])))
                    else -> messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_LOGIN)
                }
            }
        }.start()
    }
}
package kiinse.me.oneconnect.plugin.commands.oneconnect

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.apiserver.enums.PlayerStatus
import kiinse.me.oneconnect.plugin.apiserver.interfaces.PlayersData
import kiinse.me.oneconnect.plugin.commands.abstracts.MineCommand
import kiinse.me.oneconnect.plugin.commands.annotations.Command
import kiinse.me.oneconnect.plugin.commands.interfaces.MineCommandContext
import kiinse.me.oneconnect.plugin.enums.Message
import kiinse.me.oneconnect.plugin.utils.MessageUtils
import org.bukkit.entity.Player

class ChangePasswordCommand(plugin: OneConnect, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "changepassword", permission = "oneconnect.player.changepassword", disallowNonPlayer = true, parameters = 3)
    fun changePassword(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        if (playersData.getPlayerStatus(player) != PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.AUTHORIZE_ON_SERVER)
            return
        }
        val args = context.args
        val password = args[1]
        if (password != args[2]) {
            messageUtils.sendMessageWithPrefix(player, Message.PASSWORD_MISMATCH)
            return
        }
        val answer = playersData.changePassword(player, args[0], password)
        when(answer.status) {
            200 -> { messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_PASSWORD_CHANGED) }
            404 -> { messageUtils.sendMessageWithPrefix(player, Message.WRONG_PASSWORD) }
            406 -> { messageUtils.sendMessageWithPrefix(player, Message.WRONG_PASSWORD_SIZE, hashMapOf(Pair("size", answer.data.getString("message").split("than ")[1]))) }
            429 -> { messageUtils.sendMessageWithPrefix(player, Message.TOO_MANY_ATTEMPTS, hashMapOf(Pair("seconds", answer.data.getString("message").split("'")[1]))) }
            202 -> { messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_SENT) }
            else -> { messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_PASSWORD_CHANGE) }
        }
    }
}
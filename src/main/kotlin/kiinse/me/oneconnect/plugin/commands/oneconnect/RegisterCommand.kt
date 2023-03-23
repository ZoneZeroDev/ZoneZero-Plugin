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

class RegisterCommand(plugin: OneConnect, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "register", permission = "oneconnect.player.register", disallowNonPlayer = true, parameters = 2)
    fun register(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        if (playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.ALREADY_LOGGED_IN)
            return
        }
        val args = context.args
        val password = args[0]
        if (password != args[1]) {
            messageUtils.sendMessageWithPrefix(player, Message.PASSWORD_MISMATCH)
            return
        }
        val answer = playersData.registerPlayer(player, password)
        when(answer.status) {
            200 -> {
                messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_REGISTERED)
                playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
            }
            406 -> { messageUtils.sendMessageWithPrefix(player, Message.WRONG_PASSWORD_SIZE, hashMapOf(Pair("size", answer.data.getString("message").split("than ")[1]))) }
            403 -> { messageUtils.sendMessageWithPrefix(player, Message.ALREADY_REGISTERED) }
            429 -> { messageUtils.sendMessageWithPrefix(player, Message.TOO_MANY_ATTEMPTS, hashMapOf(Pair("seconds", answer.data.getString("message").split("'")[1]))) }
            else -> { messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_REGISTER) }
        }
    }
}
package kiinse.me.oneconnect.plugin.commands.oneconnect

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.apiserver.enums.PlayerStatus
import kiinse.me.oneconnect.plugin.apiserver.interfaces.PlayersData
import kiinse.me.oneconnect.plugin.commands.abstracts.MineCommand
import kiinse.me.oneconnect.plugin.commands.annotations.Command
import kiinse.me.oneconnect.plugin.commands.interfaces.MineCommandContext
import kiinse.me.oneconnect.plugin.enums.Config
import kiinse.me.oneconnect.plugin.enums.Message
import kiinse.me.oneconnect.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import org.tomlj.TomlTable

class LoginCommand(plugin: OneConnect, private val playersData: PlayersData, config: TomlTable) : MineCommand(plugin) {

    private val kickOnWrongPassword = config.getBoolean(Config.KICK_WRONG_PASSWORD.value) { false }
    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "login", permission = "oneconnect.player.login", disallowNonPlayer = true, parameters = 1)
    fun login(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        if (playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.ALREADY_LOGGED_IN)
            return
        }
        val answer = playersData.authPlayer(player, context.args[0])
        when(answer.status) {
            200 -> {
                playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
                messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_LOGGED_IN)
            }
            403 -> {
                if (kickOnWrongPassword) { player.kickPlayer(messageUtils.getOrString(Message.WRONG_PASSWORD))
                } else { messageUtils.sendMessageWithPrefix(player, Message.WRONG_PASSWORD) }
            }
            404 -> { messageUtils.sendMessageWithPrefix(player, Message.NOT_REGISTERED) }
            429 -> { messageUtils.sendMessageWithPrefix(player, Message.TOO_MANY_ATTEMPTS, hashMapOf(Pair("seconds", answer.data.getString("message")!!.split("'")[1]))) }
            202 -> { messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_SENT) }
            else -> { messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_LOGIN) }
        }
    }
}
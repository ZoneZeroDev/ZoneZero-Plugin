package kiinse.me.zonezero.plugin.commands.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.commands.abstracts.MineCommand
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import org.tomlj.TomlTable

@Suppress("unused")
class LoginCommand(plugin: ZoneZero, private val playersData: PlayersData, config: TomlTable) : MineCommand(plugin) {

    private val kickOnWrongPassword = config.getBoolean(Config.KICK_WRONG_PASSWORD.value) { false }
    private val messageUtils: MessageUtils = plugin.messageUtils

    @Command(command = "login", permission = "zonezero.player.login", disallowNonPlayer = true, parameters = 1)
    fun login(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        if (playersData.getPlayerStatus(player) == PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.ALREADY_LOGGED_IN)
            return
        }
        playersData.authPlayer(player, context.args[0]) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_LOGGED_IN)
                        playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
                    }
                    202  -> messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_SENT)
                    401  -> {
                        if (kickOnWrongPassword) {
                            player.kickPlayer(messageUtils.getOrString(player, Message.WRONG_PASSWORD))
                        } else {
                            messageUtils.sendMessageWithPrefix(player, Message.WRONG_PASSWORD)
                        }
                    }
                    404  -> messageUtils.sendMessageWithPrefix(player, Message.NOT_REGISTERED)
                    429  -> messageUtils.sendMessageWithPrefix(player, Message.TOO_MANY_ATTEMPTS, hashMapOf(Pair("seconds", answer.data.getString("message")!!.split("'")[1])))
                    else -> messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_LOGIN)
                }
            }
        }.start()
    }
}
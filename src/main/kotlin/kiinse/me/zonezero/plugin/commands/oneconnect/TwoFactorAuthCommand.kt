package kiinse.me.zonezero.plugin.commands.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.commands.abstracts.MineCommand
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.annotations.SubCommand
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.service.enums.QueryType
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import java.util.regex.Pattern

class TwoFactorAuthCommand(plugin: ZoneZero, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils
    private val emailPattern: Pattern = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" +
                                                                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." +
                                                                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+")

    @Command(command = "2fa", permission = "zonezero.player.2fa", parameters = 1, disallowNonPlayer = true)
    fun twoFa(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        val answer = playersData.codeTwoFa(player, context.args[0])
        when (answer.status) {
            405 -> { messageUtils.sendMessageWithPrefix(player, Message.NOT_REGISTERED) }
            404 -> { messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_INCORRECT) }
            200 -> {
                when (QueryType.valueOf(answer.data.getString("message"))) {
                    QueryType.AUTH -> {
                        playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
                        messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_LOGGED_IN)
                    }
                    QueryType.ENABLE_TFA -> {
                        messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_TWO_FACTOR_ENABLED)
                    }
                    QueryType.CHANGE_PASSWORD  -> {
                        messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_PASSWORD_CHANGED)
                    }
                    QueryType.DISABLE_TFA  -> {
                        messageUtils.sendMessageWithPrefix(player, Message.SUCCESSFULLY_TWO_FACTOR_DISABLED)
                    }
                }
            }
            else -> { messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_TWO_FACTOR) }
        }
    }

    @SubCommand(command = "help", permission = "zonezero.player.2fa", disallowNonPlayer = true)
    fun help(context: MineCommandContext) {
        messageUtils.sendMessage(context.sender, "&d=========== &6&l[&bZoneZero&6&l] &d===========\n", Message.TWO_FACTOR_HELP)
    }

    @SubCommand(command = "enable", permission = "zonezero.player.2fa", parameters = 3, disallowNonPlayer = true)
    fun enable(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        if (playersData.getPlayerStatus(player) != PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.AUTHORIZE_ON_SERVER)
            return
        }
        val args = context.args
        val email = args[1]
        if (email != args[2]) {
            messageUtils.sendMessageWithPrefix(player, Message.EMAIL_MISMATCH)
            return
        }
        if (!checkEmail(email)) {
            messageUtils.sendMessageWithPrefix(player, Message.NOT_EMAIL, hashMapOf(Pair("value", email)))
            return
        }
        val answer = playersData.enableTwoFa(player, email, args[0])
        when(answer.status) {
            405 -> { messageUtils.sendMessageWithPrefix(player, Message.NOT_EMAIL) }
            403 -> { messageUtils.sendMessageWithPrefix(player, Message.WRONG_PASSWORD) }
            406 -> { messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_ALREADY_ENABLED, hashMapOf(Pair("email", answer.data.getString("message")))) }
            200 -> { messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_SENT) }
            else -> { messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_TWO_FACTOR) }
        }
    }

    @SubCommand(command = "disable", permission = "zonezero.player.2fa", parameters = 1, disallowNonPlayer = true)
    fun disable(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        if (playersData.getPlayerStatus(player) != PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.AUTHORIZE_ON_SERVER)
            return
        }
        val answer = playersData.disableTwoFa(player, context.args[0])
        when(answer.status) {
            403 -> { messageUtils.sendMessageWithPrefix(player, Message.WRONG_PASSWORD) }
            429 -> { messageUtils.sendMessageWithPrefix(player, Message.TOO_MANY_ATTEMPTS, hashMapOf(Pair("seconds", answer.data.getString("message")!!.split("'")[1]))) }
            406 -> { messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_ALREADY_DISABLED) }
            200 -> { messageUtils.sendMessageWithPrefix(player, Message.TWO_FACTOR_SENT) }
            else -> { messageUtils.sendMessageWithPrefix(player, Message.ERROR_ON_TWO_FACTOR) }
        }
    }

    private fun checkEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }
}
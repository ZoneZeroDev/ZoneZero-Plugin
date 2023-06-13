package kiinse.me.zonezero.plugin.commands.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.commands.abstracts.MineCommand
import kiinse.me.zonezero.plugin.commands.annotations.Command
import kiinse.me.zonezero.plugin.commands.annotations.SubCommand
import kiinse.me.zonezero.plugin.commands.interfaces.MineCommandContext
import kiinse.me.zonezero.plugin.enums.*
import kiinse.me.zonezero.plugin.messages.MessageBuilder
import kiinse.me.zonezero.plugin.service.body.PlayerTwoFaCodeBody
import kiinse.me.zonezero.plugin.service.body.PlayerTwoFaDisableBody
import kiinse.me.zonezero.plugin.service.body.PlayerTwoFaEnableBody
import kiinse.me.zonezero.plugin.service.enums.QueryType
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@Suppress("unused")
class TwoFactorCommand(plugin: ZoneZero, private val playersData: PlayersData) : MineCommand(plugin) {

    private val messageUtils: MessageUtils = plugin.messageUtils
    private val emailPattern: Pattern = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" +
                                                                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." +
                                                                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+")

    @Command(command = "2fa", permission = "zonezero.player.2fa", parameters = 1, disallowNonPlayer = true)
    fun twoFa(context: MineCommandContext) {
        val player = context.sender as Player
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        playersData.codeTwoFa(player, PlayerTwoFaCodeBody(context.args[0], playersData.getPlayerIp(player))) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        when (QueryType.valueOf(answer.getMessage())) {
                            QueryType.AUTH            -> {
                                playersData.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
                                MessageBuilder(messageUtils, player)
                                    .setMessage(Message.SUCCESSFULLY_LOGGED_IN)
                                    .setTitle(Title.WELCOME)
                                    .setSubTitle(SubTitle.WELCOME)
                                    .setTitleType(TitleType.DISPLAY_NAME)
                                    .send()
                            }

                            QueryType.ENABLE_TFA      -> {
                                MessageBuilder(messageUtils, player)
                                    .setMessage(Message.SUCCESSFULLY_TWO_FACTOR_ENABLED)
                                    .setTitle(Title.TWO_FA_SUCCESS)
                                    .setSubTitle(SubTitle.TWO_FA_ENABLED)
                                    .send()
                            }

                            QueryType.CHANGE_PASSWORD -> {
                                MessageBuilder(messageUtils, player)
                                    .setMessage(Message.SUCCESSFULLY_PASSWORD_CHANGED)
                                    .setTitle(Title.PASSWORD_CHANGED)
                                    .setSubTitle(SubTitle.PASSWORD_CHANGED)
                                    .send()
                            }

                            QueryType.DISABLE_TFA     -> {
                                MessageBuilder(messageUtils, player)
                                    .setMessage(Message.SUCCESSFULLY_TWO_FACTOR_DISABLED)
                                    .setTitle(Title.TWO_FA_SUCCESS)
                                    .setSubTitle(SubTitle.TWO_FA_DISABLED)
                                    .send()
                            }

                            QueryType.ACCOUNT_REMOVE  -> {
                                playersData.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
                                MessageBuilder(messageUtils, player)
                                    .setMessage(Message.SUCCESSFULLY_ACCOUNT_REMOVED)
                                    .setTitle(Title.REGISTER)
                                    .setSubTitle(SubTitle.REGISTER)
                                    .setTitleTime(TimeUnit.MINUTES.toSeconds(1000).toInt())
                                    .send()
                            }
                        }

                    }

                    429  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TOO_MANY_ATTEMPTS)
                            .setReplaceMap(hashMapOf(Pair("seconds", answer.getMessage().split("'")[1])))
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
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

                    403  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.CODE_OUTDATED)
                            .setTitle(Title.TWO_FA_ERROR)
                            .setSubTitle(SubTitle.TWO_FA_EXPIRED)
                            .send()
                    }

                    406  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TWO_FACTOR_INCORRECT)
                            .setTitle(Title.TWO_FA_ERROR)
                            .setSubTitle(SubTitle.TWO_FA_INCORRECT)
                            .send()
                    }

                    else -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.ERROR_ON_TWO_FACTOR)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }
                }
            }
        }.start()
    }

    @SubCommand(command = "help", permission = "zonezero.player.2fa", disallowNonPlayer = true)
    fun help(context: MineCommandContext) {
        messageUtils.sendMessage(context.sender, Strings.ZONE_ZERO_TOP.value, Message.TWO_FACTOR_HELP)
    }

    @SubCommand(command = "enable", permission = "zonezero.player.2fa", parameters = 3, disallowNonPlayer = true)
    fun enable(context: MineCommandContext) {
        val player = context.sender as Player
        if (playersData.getPlayerStatus(player) != PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.AUTHORIZE_ON_SERVER)
            return
        }
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        val args = context.args
        val email = args[1]
        if (email != args[2]) {
            MessageBuilder(messageUtils, player)
                .setMessage(Message.EMAIL_MISMATCH)
                .setTitle(Title.EMAIL_MISMATCH)
                .setSubTitle(SubTitle.EMAIL_MISMATCH)
                .send()
            return
        }
        if (!checkEmail(email)) {
            messageUtils.sendMessageWithPrefix(player, Message.NOT_EMAIL, hashMapOf(Pair("value", email)))
            return
        }
        playersData.enableTwoFa(player, PlayerTwoFaEnableBody(args[0], email, playersData.getPlayerIp(player))) { answer ->
            run {
                when (answer.status) {
                    200  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TWO_FACTOR_SENT)
                            .setTitle(Title.TWO_FA_SEND)
                            .setSubTitle(SubTitle.TWO_FA_SEND)
                            .send()
                    }

                    405  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.NOT_EMAIL)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }

                    401  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.WRONG_PASSWORD)
                            .setTitle(Title.PASSWORD_WRONG)
                            .setSubTitle(SubTitle.PASSWORD_WRONG)
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
                            .setMessage(Message.TWO_FACTOR_ALREADY_ENABLED)
                            .setReplaceMap(hashMapOf(Pair("email", answer.getMessage())))
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }

                    else -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.ERROR_ON_TWO_FACTOR)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }
                }
            }
        }.start()
    }

    @SubCommand(command = "disable", permission = "zonezero.player.2fa", parameters = 1, disallowNonPlayer = true)
    fun disable(context: MineCommandContext) {
        val player = context.sender as Player
        if (playersData.getPlayerStatus(player) != PlayerStatus.AUTHORIZED) {
            messageUtils.sendMessageWithPrefix(player, Message.AUTHORIZE_ON_SERVER)
            return
        }
        messageUtils.sendMessageWithPrefix(player, Message.PLEASE_WAIT)
        playersData.disableTwoFa(player, PlayerTwoFaDisableBody(context.args[0], playersData.getPlayerIp(player))) { answer ->
            run {
                when (answer.status) {
                    200  -> {
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

                    403  -> {
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

                    406  -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.TWO_FACTOR_ALREADY_DISABLED)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }

                    else -> {
                        MessageBuilder(messageUtils, player)
                            .setMessage(Message.ERROR_ON_TWO_FACTOR)
                            .setTitle(Title.ERROR)
                            .setSubTitle(SubTitle.ERROR)
                            .send()
                    }
                }
            }
        }.start()
    }

    private fun checkEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }
}
package kiinse.me.zonezero.plugin.apiserver

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.service.body.*
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import kiinse.me.zonezero.plugin.service.interfaces.post
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.tomlj.TomlTable
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level
 class PlayersService(plugin: ZoneZero, private val api: ApiService, config: TomlTable) : PlayersData {

    private val joinMessageOnAuth = config.getBoolean(Config.JOIN_MESSAGE_ON_AUTH.value) { false }
    private val applyBlindEffect = config.getBoolean(Config.APPLY_BLIND_EFFECT.value) { false }
    private val joinMessages: HashMap<UUID, String> = HashMap()
    private val playersStatus: HashMap<UUID, PlayerStatus> = HashMap()
    private val filesUtils = plugin.filesUtils

    init {
        val file = filesUtils.getFile(Strings.DATA_FILE.value)
        if (file.exists()) {
            ZoneZero.sendLog(Level.CONFIG, Strings.STATUSES_LOADING.value)
            playersStatus.clear()
            file.forEachLine {
                if (it.isNotEmpty()) {
                    val raw = it.split(":")
                    playersStatus[UUID.fromString(raw[0])] = PlayerStatus.valueOf(raw[1])
                    ZoneZero.sendLog(Level.CONFIG, "Player: ${raw[0]} | Value: ${raw[1]}")
                }
            }
            ZoneZero.sendLog(Level.CONFIG, Strings.STATUSES_LOADED.value)
        }
    }

    override fun getPlayerStatus(player: Player): PlayerStatus {
        return playersStatus[player.uniqueId] ?: PlayerStatus.NOT_AUTHORIZED
    }

    @Suppress("result_unused")
    override fun setPlayerStatus(player: Player, status: PlayerStatus) {
        val uuid = player.uniqueId
        if (status == PlayerStatus.NOT_AUTHORIZED) {
            if (applyBlindEffect) { player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1000000000, 1000000000, false, false)) }
        } else {
            player.removePotionEffect(PotionEffectType.BLINDNESS)
            if (joinMessageOnAuth) {
                runBlocking {
                    async { sendAllJoinMessage(getJoinMessage(player)) }.start()
                    async { removeJoinMessage(player) }.start()
                }
            }
        }
        playersStatus[uuid] = status
    }

    private fun sendAllJoinMessage(message: String) {
        if (message.isEmpty()) return
        Bukkit.getServer().onlinePlayers.forEach {
            it.sendMessage(message)
        }
    }

    private fun getJoinMessage(player: Player): String {
        return joinMessages[player.uniqueId] ?: ""
    }

    override fun addJoinMessage(player: Player, string: String) {
        joinMessages[player.uniqueId] = string
    }

    override fun removeJoinMessage(player: Player) {
        joinMessages.remove(player.uniqueId)
    }

    override fun authPlayer(player: Player, body: PlayerLoginBody, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async {
            consumer.accept(api.post<PlayerLoginBody>(ServerAddress.LOGIN_PLAYER, body, player))
        }
    }

    override fun removePlayer(player: Player, body: PlayerRemoveBody, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async { consumer.accept(api.post<PlayerRemoveBody>(ServerAddress.REMOVE_PLAYER, body, player)) }
    }

    override fun changePassword(player: Player, body: PlayerPasswordChangeBody, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async { consumer.accept(api.post<PlayerPasswordChangeBody>(ServerAddress.CHANGE_PASSWORD, body, player)) }
    }

    override fun authPlayerByIp(player: Player, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async { consumer.accept(api.post<PlayerLoginBody>(ServerAddress.LOGIN_PLAYER_BY_IP,
                                                                             PlayerLoginBody(ip = getPlayerIp(player)),
                                                                             player)) }
    }

    override fun registerPlayer(player: Player, body: PlayerRegisterBody, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async { consumer.accept(api.post<PlayerRegisterBody>(ServerAddress.REGISTER_PLAYER, body, player)) }
    }

    override fun enableTwoFa(player: Player, body: PlayerTwoFaEnableBody, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async { consumer.accept(api.post<PlayerTwoFaEnableBody>(ServerAddress.TWO_FA_ENABLE, body, player)) }
    }

    override fun disableTwoFa(player: Player, body: PlayerTwoFaDisableBody, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async { consumer.accept(api.post<PlayerTwoFaDisableBody>(ServerAddress.TWO_FA_DISABLE, body, player)) }
    }

    override fun codeTwoFa(player: Player, body: PlayerTwoFaCodeBody, consumer: Consumer<ServerAnswer>): Deferred<Unit> = runBlocking {
        return@runBlocking async { consumer.accept(api.post<PlayerTwoFaCodeBody>(ServerAddress.TWO_FA_CODE, body, player)) }
    }

    override fun getPlayerIp(player: Player): String {
        return player.address?.address?.hostAddress ?: Strings.STRING_NULL.value
    }

    override fun savePlayersStatuses() {
        val file = filesUtils.getFile(Strings.DATA_FILE.value)
        val builder = StringBuilder()
        playersStatus.forEach { (key, value) ->
            builder.append(key.toString()).append(":").append(value.toString()).append("\n")
        }
        file.writeText(builder.toString(), Charsets.UTF_8)
    }
}
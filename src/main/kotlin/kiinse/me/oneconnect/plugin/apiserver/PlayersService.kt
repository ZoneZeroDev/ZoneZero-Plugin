package kiinse.me.oneconnect.plugin.apiserver

import kiinse.me.oneconnect.plugin.apiserver.interfaces.PlayersData
import kiinse.me.oneconnect.plugin.apiserver.enums.PlayerStatus
import kiinse.me.oneconnect.plugin.enums.Config
import kiinse.me.oneconnect.plugin.service.ServerAnswer
import kiinse.me.oneconnect.plugin.service.enums.ServerAddress
import kiinse.me.oneconnect.plugin.service.interfaces.ApiService
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.json.JSONObject
import org.tomlj.TomlTable
import java.util.*
import kotlin.collections.HashMap
import kotlin.jvm.Throws

class PlayersService(private val api: ApiService, config: TomlTable) : PlayersData {

    private val applyBlindEffect = config.getBoolean(Config.APPLY_BLIND_EFFECT.value) { false }
    private val playersStatus: HashMap<UUID, PlayerStatus> = HashMap()

    init {
        // TODO: Загрузка playersStatus с файла
    }

    override fun getPlayerStatus(player: Player): PlayerStatus {
        val uuid = player.uniqueId
        if (!playersStatus.containsKey(uuid)) {
            return PlayerStatus.NOT_AUTHORIZED
        }
        return playersStatus[uuid]!!
    }

    override fun setPlayerStatus(player: Player, status: PlayerStatus) {
        val uuid = player.uniqueId
        if (status == PlayerStatus.NOT_AUTHORIZED) {
            if (applyBlindEffect) { player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 1000000000, 1000000000, false, false)) }
        } else {
            player.removePotionEffect(PotionEffectType.BLINDNESS)
        }
        if (playersStatus.containsKey(uuid)) {
            playersStatus.replace(uuid, status)
            return
        }
        playersStatus[uuid] = status
    }

    override fun authPlayer(player: Player, password: String): ServerAnswer {
        return api.post(ServerAddress.LOGIN_PLAYER, getPlayerInfo(player, password))
    }

    override fun changePassword(player: Player, oldPassword: String, newPassword: String): ServerAnswer {
        val json = JSONObject()
        json.put("login", player.name)
        json.put("oldPassword", oldPassword)
        json.put("newPassword", newPassword)
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.CHANGE_PASSWORD,json)
    }

    override fun authPlayerByIp(player: Player): ServerAnswer {
        val json = JSONObject()
        json.put("login", player.name)
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.LOGIN_PLAYER_BY_IP, json)
    }

    override fun registerPlayer(player: Player, password: String): ServerAnswer {
        return api.post(ServerAddress.REGISTER_PLAYER, getPlayerInfo(player, password))
    }

    override fun enableTwoFa(player: Player, email: String, password: String): ServerAnswer {
        val json = JSONObject()
        json.put("login", player.name)
        json.put("password", password)
        json.put("email", email)
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.TWO_FA_ENABLE, json)
    }

    override fun disableTwoFa(player: Player, password: String): ServerAnswer {
        val json = JSONObject()
        json.put("login", player.name)
        json.put("password", password)
        return api.post(ServerAddress.TWO_FA_DISABLE, json)
    }

    override fun codeTwoFa(player: Player, code: String): ServerAnswer {
        val json = JSONObject()
        json.put("login", player.name)
        json.put("code", code)
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.TWO_FA_CODE, json)
    }

    private fun getPlayerInfo(player: Player, password: String): JSONObject {
        val json = JSONObject()
        json.put("login", player.name)
        json.put("password", password)
        json.put("ip", getPlayerIp(player))
        return json
    }

    private fun getPlayerIp(player: Player): String {
        return player.address?.address?.hostAddress ?: "null"
    }

    override fun savePlayersStatuses() {
        TODO("Not yet implemented")
    }
}
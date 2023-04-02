package kiinse.me.zonezero.plugin.apiserver

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.service.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import kiinse.me.zonezero.plugin.utils.FilesUtils
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.json.JSONObject
import org.tomlj.TomlTable
import java.util.*
import java.util.logging.Level

class PlayersService(plugin: ZoneZero, private val api: ApiService, config: TomlTable) : PlayersData {

    private val applyBlindEffect = config.getBoolean(Config.APPLY_BLIND_EFFECT.value) { false }
    private val playersStatus: HashMap<UUID, PlayerStatus> = HashMap()
    private val filesUtils = plugin.filesUtils

    init {
        val file = filesUtils.getFile("data.zz")
        if (file.exists()) {
            ZoneZero.sendLog(Level.CONFIG, "Loading player statuses...")
            playersStatus.clear()
            file.forEachLine {
                if (it.isNotEmpty()) {
                    val raw = it.split(":")
                    playersStatus[UUID.fromString(raw[0])] = PlayerStatus.valueOf(raw[1])
                    ZoneZero.sendLog(Level.CONFIG, "Player: ${raw[0]} | Value: ${raw[1]}")
                }
            }
            ZoneZero.sendLog(Level.CONFIG, "Player statuses has been loaded!")
        }
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
        return api.post(ServerAddress.LOGIN_PLAYER, getPlayerInfo(player, password), player)
    }

    override fun changePassword(player: Player, oldPassword: String, newPassword: String): ServerAnswer {
        val json = JSONObject()
        json.put("oldPassword", oldPassword)
        json.put("newPassword", newPassword)
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.CHANGE_PASSWORD, json, player)
    }

    override fun authPlayerByIp(player: Player): ServerAnswer {
        val json = JSONObject()
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.LOGIN_PLAYER_BY_IP, json, player)
    }

    override fun registerPlayer(player: Player, password: String): ServerAnswer {
        return api.post(ServerAddress.REGISTER_PLAYER, getPlayerInfo(player, password), player)
    }

    override fun enableTwoFa(player: Player, email: String, password: String): ServerAnswer {
        val json = JSONObject()
        json.put("password", password)
        json.put("email", email)
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.TWO_FA_ENABLE, json, player)
    }

    override fun disableTwoFa(player: Player, password: String): ServerAnswer {
        return api.post(ServerAddress.TWO_FA_DISABLE, getPlayerInfo(player, password), player)
    }

    override fun codeTwoFa(player: Player, code: String): ServerAnswer {
        val json = JSONObject()
        json.put("code", code)
        json.put("ip", getPlayerIp(player))
        return api.post(ServerAddress.TWO_FA_CODE, json, player)
    }

    private fun getPlayerInfo(player: Player, password: String): JSONObject {
        val json = JSONObject()
        json.put("password", password)
        json.put("ip", getPlayerIp(player))
        return json
    }

    private fun getPlayerIp(player: Player): String {
        return player.address?.address?.hostAddress ?: "null"
    }

    override fun savePlayersStatuses() {
        val file = filesUtils.getFile("data.zz")
        val builder = StringBuilder()
        playersStatus.forEach { (key, value) ->
            builder.append(key.toString()).append(":").append(value.toString()).append("\n")
        }
        file.writeText(builder.toString(), Charsets.UTF_8)
    }
}
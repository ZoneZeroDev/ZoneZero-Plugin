package kiinse.me.zonezero.plugin.service.interfaces

import kiinse.me.zonezero.plugin.service.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import org.bukkit.entity.Player
import org.json.JSONObject

interface ApiService {

    fun get(address: ServerAddress): ServerAnswer
    fun post(address: ServerAddress, data: JSONObject): ServerAnswer
    fun get(address: ServerAddress, player: Player): ServerAnswer
    fun post(address: ServerAddress, data: JSONObject, player: Player): ServerAnswer
    fun updateServerKey()

}
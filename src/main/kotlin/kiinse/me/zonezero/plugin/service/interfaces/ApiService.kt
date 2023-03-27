package kiinse.me.zonezero.plugin.service.interfaces

import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.ServerAnswer
import org.json.JSONObject

interface ApiService {

    fun get(address: ServerAddress): ServerAnswer
    fun post(address: ServerAddress, data: JSONObject): ServerAnswer
    fun updateServerKey()

}
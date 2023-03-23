package kiinse.me.oneconnect.plugin.service.interfaces

import kiinse.me.oneconnect.plugin.service.enums.ServerAddress
import kiinse.me.oneconnect.plugin.service.ServerAnswer
import org.json.JSONObject

interface ApiService {

    fun get(address: ServerAddress): ServerAnswer
    fun post(address: ServerAddress, data: JSONObject): ServerAnswer
    fun updateServerKey()

}
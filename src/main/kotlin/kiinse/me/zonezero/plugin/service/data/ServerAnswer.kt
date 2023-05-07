package kiinse.me.zonezero.plugin.service.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class ServerAnswer(val address: String, val status: Int, val data: String) {
    fun getMessageAnswer(): MessageAnswer {
        return Json.decodeFromString<MessageAnswer>(data)
    }
}

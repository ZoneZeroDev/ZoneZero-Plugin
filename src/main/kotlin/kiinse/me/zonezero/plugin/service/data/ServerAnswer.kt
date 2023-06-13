package kiinse.me.zonezero.plugin.service.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.reflect.InvocationTargetException

data class ServerAnswer(val address: String, val status: Int, val data: String) {

    fun getMessage(): String {
        val message = try {
            Json.decodeFromString<MessageAnswer>(data).message
        } catch (e: InvocationTargetException) {
            Json.decodeFromString<ExceptionAnswer>(data).message
        }
        return message
    }
}

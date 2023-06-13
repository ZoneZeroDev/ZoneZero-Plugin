package kiinse.me.zonezero.plugin.service.interfaces

import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json.Default.serializersModule
import kotlinx.serialization.serializer
import org.bukkit.entity.Player

interface ApiService {

    fun get(address: ServerAddress): ServerAnswer
    fun get(address: ServerAddress, player: Player): ServerAnswer
    fun <T> post(address: ServerAddress, strategy: SerializationStrategy<T>, value: T): ServerAnswer
    fun <T> post(address: ServerAddress, strategy: SerializationStrategy<T>, value: T, player: Player): ServerAnswer

    fun updateServerKey()

}

inline fun <reified T> ApiService.post(address: ServerAddress, value: T): ServerAnswer {
    return post(address, serializersModule.serializer(), value)
}

inline fun <reified T> ApiService.post(address: ServerAddress, value: T, player: Player): ServerAnswer {
    return post(address, serializersModule.serializer(), value, player)
}
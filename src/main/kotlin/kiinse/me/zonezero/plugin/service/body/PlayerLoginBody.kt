package kiinse.me.zonezero.plugin.service.body

import kotlinx.serialization.Serializable

@Serializable
data class PlayerLoginBody(val password: String? = "",
                           val ip: String)

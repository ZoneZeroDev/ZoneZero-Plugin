package kiinse.me.zonezero.plugin.service.body

import kotlinx.serialization.Serializable

@Serializable
data class PlayerTwoFaEnableBody(val password: String,
                                 val email: String,
                                 val ip: String? = "")
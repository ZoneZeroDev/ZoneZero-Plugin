package kiinse.me.zonezero.plugin.service.body

import kotlinx.serialization.Serializable

@Serializable
data class PlayerTwoFaCodeBody(val code: String,
                               val ip: String? = "")

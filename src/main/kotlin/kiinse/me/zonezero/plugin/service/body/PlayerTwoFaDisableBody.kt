package kiinse.me.zonezero.plugin.service.body

import kotlinx.serialization.Serializable

@Serializable
data class PlayerTwoFaDisableBody(val password: String,
                                  val ip: String? = "")

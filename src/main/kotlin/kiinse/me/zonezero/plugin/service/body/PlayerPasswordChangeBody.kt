package kiinse.me.zonezero.plugin.service.body

import kotlinx.serialization.Serializable

@Serializable
data class PlayerPasswordChangeBody(val oldPassword: String,
                                    val newPassword: String,
                                    val ip: String? = "")

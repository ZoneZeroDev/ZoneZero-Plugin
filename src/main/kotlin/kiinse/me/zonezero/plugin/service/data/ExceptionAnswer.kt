package kiinse.me.zonezero.plugin.service.data

import kotlinx.serialization.Serializable

@Serializable
data class ExceptionAnswer(val exceptionClass: String = "", val message: String = "")
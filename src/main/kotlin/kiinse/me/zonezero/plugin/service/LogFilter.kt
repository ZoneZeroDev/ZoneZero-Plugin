package kiinse.me.zonezero.plugin.service

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.filter.AbstractFilter
import org.apache.logging.log4j.message.Message


class LogFilter : AbstractFilter() {

    private val denyCommands: Set<String> = hashSetOf("/login", "/lg", "/log", "/rg", "/reg", "/register", "/changepassword", "/cp", "/changepass", "/chpass", "/chpassword", "/2fa", "/2a", "/2f")

    override fun filter(event: LogEvent?): Filter.Result {
        return if (event == null) Filter.Result.NEUTRAL else isLoggable(event.message.formattedMessage)
    }

    override fun filter(logger: Logger?, level: Level?, marker: Marker?, msg: Message, t: Throwable?): Filter.Result {
        return isLoggable(msg.formattedMessage)
    }

    override fun filter(logger: Logger?, level: Level?, marker: Marker?, msg: String?, vararg params: Any?): Filter.Result {
        return isLoggable(msg)
    }

    override fun filter(logger: Logger?, level: Level?, marker: Marker?, msg: Any?, t: Throwable?): Filter.Result {
        return if (msg == null) Filter.Result.NEUTRAL else isLoggable(msg.toString())
    }

    private fun isLoggable(msg: String?): Filter.Result {
        if (msg != null && msg.contains("issued server command:", ignoreCase = true)) {
            denyCommands.forEach {
                if (msg.contains(it, ignoreCase = true)) {
                    return Filter.Result.DENY
                }
            }
        }
        return Filter.Result.NEUTRAL
    }
}
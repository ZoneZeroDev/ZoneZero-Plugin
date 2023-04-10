package kiinse.me.zonezero.plugin.utils

import com.vdurmont.semver4j.Semver
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.exceptions.VersioningException
import org.apache.http.client.fluent.Request
import java.io.IOException
import java.util.*
import java.util.function.Consumer

object VersionUtils {

    private const val timeout = 5000

    @Throws(VersioningException::class)
    fun getLatestSpigotVersion(consumer: Consumer<Semver>) {
        consumer.accept(Semver(getLatestSpigotVersionAsString()))
    }

    @Throws(VersioningException::class)
    private fun getLatestSpigotVersionAsString(): String {
        try {
            val request = Request.Get(Strings.SPIGOT_URL.value)
            request.connectTimeout(timeout)
            request.socketTimeout(timeout)
            request.execute().returnContent()
            return request.execute().returnContent().asString().replace("×", "")
        } catch (e: IOException) {
            throw VersioningException(Strings.VERSION_ERROR.value, e)
        }
    }

    fun getPluginVersion(plugin: ZoneZero): Semver {
        return Semver(plugin.description.version)
    }
}
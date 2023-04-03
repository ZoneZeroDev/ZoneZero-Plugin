package kiinse.me.zonezero.plugin.utils

import com.vdurmont.semver4j.Semver
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.exceptions.VersioningException
import org.apache.http.client.fluent.Request
import java.io.IOException
import java.util.*
import java.util.function.Consumer

object VersionUtils {

    @Throws(VersioningException::class)
    fun getLatestSpigotVersion(consumer: Consumer<Semver>) {
        consumer.accept(Semver(getLatestSpigotVersionAsString()))
    }

    @Throws(VersioningException::class)
    private fun getLatestSpigotVersionAsString(): String {
        try {
            return Request.Get("https://api.spigotmc.org/legacy/update.php?resource=108992")
                .connectTimeout(3000)
                .socketTimeout(3000)
                .execute()
                .returnContent()
                .asString()
                .replace("×", "")
        } catch (e: IOException) {
            throw VersioningException("Failed to get the latest version SpigotMC", e)
        }
    }

    fun getPluginVersion(plugin: ZoneZero): Semver {
        return Semver(plugin.description.version)
    }
}
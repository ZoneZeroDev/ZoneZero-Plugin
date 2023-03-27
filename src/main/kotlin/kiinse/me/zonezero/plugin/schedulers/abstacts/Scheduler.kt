package kiinse.me.zonezero.plugin.schedulers.abstacts

import kiinse.me.zonezero.plugin.ZoneZero
import org.apache.commons.lang3.RandomStringUtils
import java.util.logging.Level

abstract class Scheduler protected constructor(val plugin: ZoneZero) {

    var name: String? = null
        set(name) {
            field = if (name.isNullOrBlank()) {
                RandomStringUtils.randomAscii(60).replace(">", "")
            } else { name }
        }
    var delay: Long = -1
    var period: Long = -1
    val isStarted: Boolean
        get() = plugin.server.scheduler.isCurrentlyRunning(schedulerID)
    private var schedulerID = 0

    open fun canStart(): Boolean {
        return true
    }

    fun start() {
        if (canStart()) {
            schedulerID = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, { this.run() }, delay, period)
            ZoneZero.sendLog(Level.CONFIG, "Scheduler '&d$name&6' started!")
            return
        }
        ZoneZero.sendLog(Level.CONFIG, "Scheduler '&d$name&6' cannot be started because the '&dcanStart()&6' method returns &cfalse")
    }

    fun stop() {
        if (isStarted) {
            plugin.server.scheduler.cancelTask(schedulerID)
            ZoneZero.sendLog(Level.CONFIG, "Scheduler '&d$name&6' stopped!")
        }
    }

    abstract fun run()
}
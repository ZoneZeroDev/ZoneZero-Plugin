package kiinse.me.zonezero.plugin.schedulers.abstacts

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.enums.Replace
import kiinse.me.zonezero.plugin.enums.Strings
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
            ZoneZero.sendLog(Level.CONFIG, Strings.SCHEDULER_STARTED.value
                .replace(Replace.SCHEDULER.value, name.toString(), ignoreCase = true))
            return
        }
        ZoneZero.sendLog(Level.CONFIG, Strings.SCHEDULER_CANT_START.value
            .replace(Replace.SCHEDULER.value, name.toString(), ignoreCase = true))
    }

    fun stop() {
        if (isStarted) {
            plugin.server.scheduler.cancelTask(schedulerID)
            ZoneZero.sendLog(Level.CONFIG, Strings.SCHEDULER_STOPPED.value
                .replace(Replace.SCHEDULER.value, name.toString(), ignoreCase = true))
        }
    }

    abstract fun run()
}
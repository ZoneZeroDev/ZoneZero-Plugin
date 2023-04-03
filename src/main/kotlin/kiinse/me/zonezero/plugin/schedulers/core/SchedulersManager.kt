package kiinse.me.zonezero.plugin.schedulers.core

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.exceptions.SchedulerException
import kiinse.me.zonezero.plugin.schedulers.abstacts.Scheduler
import kiinse.me.zonezero.plugin.schedulers.annotations.SchedulerData
import kiinse.me.zonezero.plugin.schedulers.interfaces.MineSchedulersManager
import kiinse.me.zonezero.plugin.service.enums.Replace
import java.util.logging.Level

class SchedulersManager : MineSchedulersManager {

    private val schedulers: MutableSet<Scheduler> = HashSet()

    override fun register(scheduler: Scheduler): MineSchedulersManager {
        val schedule: Scheduler = checkSchedulerData(scheduler)
        if (hasScheduler(schedule)) throw SchedulerException(Strings.SCHEDULER_ALREADY_EXISTS.value
            .replace(Replace.SCHEDULER.value, scheduler.name.toString(), ignoreCase = true))
        schedulers.add(scheduler)
        ZoneZero.sendLog(Level.CONFIG, Strings.SCHEDULER_REGISTERED.value
            .replace(Replace.SCHEDULER.value, scheduler.name.toString(), ignoreCase = true)
            .replace(Replace.PLUGIN.value, scheduler.plugin.name, ignoreCase = true))
        scheduler.start()
        return this
    }

    @Throws(SchedulerException::class)
    override fun startScheduler(scheduler: Scheduler): MineSchedulersManager {
        schedulers.forEach {
            if (it.name == scheduler.name) {
                if (it.isStarted) throw SchedulerException(Strings.SCHEDULER_ALREADY_STARTED.value
                    .replace(Replace.SCHEDULER.value, scheduler.name.toString(), ignoreCase = true))
                it.start()
            }
        }
        return this
    }

    @Throws(SchedulerException::class)
    override fun stopScheduler(scheduler: Scheduler): MineSchedulersManager {
        schedulers.forEach {
            if (it.name == scheduler.name) {
                if (!it.isStarted) throw SchedulerException(Strings.SCHEDULER_ALREADY_STOPPED.value
                    .replace(Replace.SCHEDULER.value, scheduler.name.toString(), ignoreCase = true))
                it.stop()
            }
        }
        return this
    }

    override fun stopSchedulers(): MineSchedulersManager {
        schedulers.forEach { it.stop() }
        return this
    }

    override fun unregisterSchedulers(): MineSchedulersManager {
        schedulers.forEach {
            unregister(it)
        }
        return this
    }

    override fun hasScheduler(scheduler: Scheduler): Boolean {
        schedulers.forEach { if (it.name == scheduler.name) return true }
        return false
    }

    override fun getSchedulerByName(name: String): Scheduler? {
        schedulers.forEach { if (it.name == name) return it }
        return null
    }

    @Throws(SchedulerException::class)
    override fun unregister(scheduler: Scheduler): MineSchedulersManager {
        if (!hasScheduler(scheduler)) throw SchedulerException(Strings.SCHEDULER_NOT_FOUND.value
            .replace(Replace.SCHEDULER.value, scheduler.name.toString(), ignoreCase = true))
        schedulers.forEach {
            if (it.name == scheduler.name) {
                if (it.isStarted) it.stop()
                schedulers.remove(scheduler)
            }
        }
        ZoneZero.sendLog(Level.CONFIG, Strings.SCHEDULER_UNREGISTERED.value
            .replace(Replace.SCHEDULER.value, scheduler.name.toString(), ignoreCase = true)
            .replace(Replace.PLUGIN.value, scheduler.plugin.name, ignoreCase = true))
        return this
    }

    private fun checkSchedulerData(scheduler: Scheduler): Scheduler {
        val annotation = scheduler.javaClass.getAnnotation(SchedulerData::class.java)
        if (scheduler.name == null) scheduler.name = annotation.name
        if (scheduler.delay <= -1) scheduler.delay = annotation.delay
        if (scheduler.period <= -1) scheduler.period = annotation.period
        return scheduler
    }

    override val allSchedulers: Set<Scheduler>
        get() = HashSet(schedulers)
}
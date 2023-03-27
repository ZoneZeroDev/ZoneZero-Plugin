package kiinse.me.zonezero.plugin.schedulers.interfaces

import kiinse.me.zonezero.plugin.exceptions.SchedulerException
import kiinse.me.zonezero.plugin.schedulers.abstacts.Scheduler


@Suppress("unused")
interface MineSchedulersManager {
    @Throws(SchedulerException::class) fun register(scheduler: Scheduler): MineSchedulersManager
    @Throws(SchedulerException::class) fun startScheduler(scheduler: Scheduler): MineSchedulersManager
    @Throws(SchedulerException::class) fun stopScheduler(scheduler: Scheduler): MineSchedulersManager
    fun stopSchedulers(): MineSchedulersManager
    fun unregisterSchedulers(): MineSchedulersManager
    fun hasScheduler(scheduler: Scheduler): Boolean
    fun getSchedulerByName(name: String): Scheduler?
    @Throws(SchedulerException::class) fun unregister(scheduler: Scheduler): MineSchedulersManager
    val allSchedulers: Set<Scheduler?>
}
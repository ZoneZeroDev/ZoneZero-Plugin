package kiinse.me.oneconnect.plugin.schedulers.annotations

@Suppress("unused")
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SchedulerData(val name: String = "", val delay: Long = 0L, val period: Long = 20L)
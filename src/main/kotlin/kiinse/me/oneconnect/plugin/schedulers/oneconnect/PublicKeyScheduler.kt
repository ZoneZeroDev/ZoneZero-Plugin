package kiinse.me.oneconnect.plugin.schedulers.oneconnect

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.schedulers.abstacts.Scheduler
import kiinse.me.oneconnect.plugin.schedulers.annotations.SchedulerData
import kiinse.me.oneconnect.plugin.service.interfaces.ApiService
import java.util.logging.Level

@SchedulerData(name = "PublicKeyScheduler", period = 100L)
class PublicKeyScheduler(plugin: OneConnect, private val api: ApiService) : Scheduler(plugin) {

    override fun run() {
        try {
            api.updateServerKey()
            OneConnect.sendLog(Level.CONFIG, "Server key updated!")
        } catch (e: Exception) {
            OneConnect.sendLog(Level.SEVERE, "Error on updating server key! Message: ", e)
        }
    }
}
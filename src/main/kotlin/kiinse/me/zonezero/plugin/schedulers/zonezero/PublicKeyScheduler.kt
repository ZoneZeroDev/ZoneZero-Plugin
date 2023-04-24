package kiinse.me.zonezero.plugin.schedulers.zonezero

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.schedulers.abstacts.Scheduler
import kiinse.me.zonezero.plugin.schedulers.annotations.SchedulerData
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import java.util.logging.Level

@SchedulerData(name = "PublicKeyScheduler", period = 100L)
class PublicKeyScheduler(plugin: ZoneZero, private val api: ApiService) : Scheduler(plugin) {

    override fun run() {
        try {
            api.updateServerKey()
        } catch (e: Exception) {
            ZoneZero.sendLog(Level.SEVERE, Strings.SERVER_KEY_UPDATE_ERROR.value, e)
        }
    }
}
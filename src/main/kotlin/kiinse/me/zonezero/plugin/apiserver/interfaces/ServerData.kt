package kiinse.me.zonezero.plugin.apiserver.interfaces

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
 interface ServerData {

    fun getPluginCode(zoneZero: ZoneZero): ServerAnswer
}
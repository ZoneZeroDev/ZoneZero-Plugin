package kiinse.me.zonezero.plugin.apiserver.interfaces

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.service.ServerAnswer

interface ServerData {

    fun isPluginRegistered(zoneZero: ZoneZero): Boolean
    fun getPluginCode(zoneZero: ZoneZero): String
    fun isServerAllowed(zoneZero: ZoneZero): ServerAnswer
    fun isTokenValid(): Boolean
}
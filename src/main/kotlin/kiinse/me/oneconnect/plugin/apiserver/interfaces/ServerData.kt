package kiinse.me.oneconnect.plugin.apiserver.interfaces

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.service.ServerAnswer

interface ServerData {

    fun isPluginRegistered(oneConnect: OneConnect): Boolean
    fun getPluginCode(oneConnect: OneConnect): String
    fun isServerAllowed(oneConnect: OneConnect): ServerAnswer
    fun isTokenValid(): Boolean
}
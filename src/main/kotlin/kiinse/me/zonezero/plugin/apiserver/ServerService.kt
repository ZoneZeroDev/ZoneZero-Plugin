package kiinse.me.zonezero.plugin.apiserver

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.interfaces.ServerData
import kiinse.me.zonezero.plugin.service.body.ServerInfoBody
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import kiinse.me.zonezero.plugin.service.interfaces.post
import org.bukkit.Bukkit
import org.bukkit.Server
 class ServerService(private val api: ApiService, private val serverName: String) : ServerData {

    override fun getPluginCode(zoneZero: ZoneZero): ServerAnswer {
        return api.post<ServerInfoBody>(ServerAddress.GET_CODE, getServerInfo(zoneZero))
    }

    private fun getWorlds(server: Server): Set<String> {
        val worlds = HashSet<String>()
        server.worlds.forEach { worlds.add(it.name) }
        return worlds
    }

    private fun getServerInfo(zoneZero: ZoneZero): ServerInfoBody {
        val server = zoneZero.server
        return ServerInfoBody(serverName.ifEmpty { server.name },
                              server.maxPlayers,
                              zoneZero.description.version,
                              server.allowEnd,
                              server.allowNether,
                              server.allowFlight,
                              Bukkit.getVersion(),
                              server.monsterSpawnLimit,
                              server.ip,
                              server.motd,
                              server.port,
                              server.worldType,
                              server.generateStructures,
                              server.spawnRadius,
                              server.viewDistance,
                              getWorlds(server))
    }
}
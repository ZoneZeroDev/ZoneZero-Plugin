package kiinse.me.zonezero.plugin.apiserver

import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.interfaces.ServerData
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.ApiService
import org.bukkit.Bukkit
import org.bukkit.Server
import org.json.JSONArray
import org.json.JSONObject

class ServerService(private val api: ApiService) : ServerData {

    override fun getPluginCode(zoneZero: ZoneZero): ServerAnswer {
        return api.post(ServerAddress.GET_CODE, getServerInfo(zoneZero))
    }

    private fun getWorlds(server: Server): Set<String> {
        val worlds = HashSet<String>()
        server.worlds.forEach { worlds.add(it.name) }
        return worlds
    }

    private fun getServerInfo(zoneZero: ZoneZero): JSONObject {
        val json = JSONObject()
        val server = zoneZero.server
        json.put("name", server.name)
        json.put("maxPlayers", server.maxPlayers)
        json.put("pluginVersion", zoneZero.description.version)
        json.put("allowEnd", server.allowEnd)
        json.put("allowNether", server.allowNether)
        json.put("allowFlight", server.allowFlight)
        json.put("bukkitVersion", Bukkit.getVersion())
        json.put("monsterSpawnLimit", server.monsterSpawnLimit)
        json.put("settingsIp", server.ip)
        json.put("motd", server.motd)
        json.put("settingsPort", server.port)
        json.put("worldType", server.worldType)
        json.put("generateStructures", server.generateStructures)
        json.put("spawnRadius", server.spawnRadius)
        json.put("viewDistance", server.viewDistance)
        json.put("worlds", JSONArray(getWorlds(server)))
        return json
    }
}
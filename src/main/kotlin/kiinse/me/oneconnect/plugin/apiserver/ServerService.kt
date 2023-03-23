package kiinse.me.oneconnect.plugin.apiserver

import kiinse.me.oneconnect.plugin.OneConnect
import kiinse.me.oneconnect.plugin.apiserver.interfaces.ServerData
import kiinse.me.oneconnect.plugin.enums.Config
import kiinse.me.oneconnect.plugin.service.ServerAnswer
import kiinse.me.oneconnect.plugin.service.enums.ServerAddress
import kiinse.me.oneconnect.plugin.service.interfaces.ApiService
import org.bukkit.Server
import org.json.JSONArray
import org.json.JSONObject
import org.tomlj.TomlTable

class ServerService(private val api: ApiService) : ServerData {

    override fun isPluginRegistered(oneConnect: OneConnect): Boolean {
        return api.post(ServerAddress.IS_PLUGIN_REGISTERED, getServerInfo(oneConnect)).status == 200
    }

    override fun getPluginCode(oneConnect: OneConnect): String {
        val answer = api.post(ServerAddress.GET_CODE, getServerInfo(oneConnect))
        if (answer.status != 200) {
            return "ERROR"
        }
        return answer.data.getString("message")
    }

    override fun isServerAllowed(oneConnect: OneConnect): ServerAnswer {
        return api.post(ServerAddress.IS_SERVER_ALLOWED, getServerCore(oneConnect))
    }

    override fun isTokenValid(): Boolean {
        return api.get(ServerAddress.TEST_TOKEN).status == 200
    }

    private fun getWorlds(server: Server): Set<String> {
        val worlds = HashSet<String>()
        server.worlds.forEach { worlds.add(it.name) }
        return worlds
    }

    private fun getServerInfo(oneConnect: OneConnect): JSONObject {
        val json = JSONObject()
        val server = oneConnect.server
        json.put("name", server.name)
        json.put("maxPlayers", server.maxPlayers)
        json.put("allowEnd", server.allowEnd)
        json.put("allowNether", server.allowNether)
        json.put("allowFlight", server.allowFlight)
        json.put("bukkitVersion", server.bukkitVersion)
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

    private fun getServerCore(oneConnect: OneConnect): JSONObject {
        val json = JSONObject()
        val server = oneConnect.server
        json.put("name", server.name)
        json.put("bukkitVersion", server.bukkitVersion)
        return json
    }
}
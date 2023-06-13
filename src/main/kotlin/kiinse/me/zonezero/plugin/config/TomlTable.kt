package kiinse.me.zonezero.plugin.config

import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import org.tomlj.TomlArray
import org.tomlj.TomlTable

class TomlTable(config: TomlTable) : Toml(config) {

    fun getString(key: ConfigKey): String {
        return config.getString(key.value) { "" }
    }

    fun getBoolean(key: ConfigKey): Boolean {
        return config.getBoolean(key.value) { false }
    }

    fun getArray(key: ConfigKey): TomlArray {
        return config.getArrayOrEmpty(key.value)
    }
}
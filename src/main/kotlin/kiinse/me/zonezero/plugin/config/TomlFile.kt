package kiinse.me.zonezero.plugin.config

import kiinse.me.zonezero.plugin.config.enums.ConfigTable

open class TomlFile(config: org.tomlj.TomlTable) : Toml(config) {

    open fun getTable(table: ConfigTable): TomlTable {
        return TomlTable(config.getTableOrEmpty(table.value))
    }
}
package kiinse.me.zonezero.plugin.config

import kiinse.me.zonezero.plugin.config.enums.ConfigKey
import org.tomlj.TomlTable
import java.util.function.Supplier

open class Toml(val config: TomlTable) {

    inline fun <reified T> get(key: ConfigKey, defaultValue: Supplier<T>): T {
        return (config.get(key.value) ?: return defaultValue.get()) as T
    }

    inline fun <reified T> get(key: ConfigKey): T? {
        return (config.get(key.value) ?: return null) as T
    }
}
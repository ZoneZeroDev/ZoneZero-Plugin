package kiinse.me.zonezero.plugin.service.enums

enum class Replace(val value: String) {
    SCHEDULER("<scheduler>"),
    PLUGIN("<plugin>"),
    FILE("<file>"),
    DIRECTORY("<directory>"),
    OLD_FILE("<old_file>"),
    CLASS("<class>"),
    COMMAND("<command>"),
    VERSION_NEW("{NEW_VERSION}"),
    VERSION_CURRENT("{CURRENT_VERSION}")
}
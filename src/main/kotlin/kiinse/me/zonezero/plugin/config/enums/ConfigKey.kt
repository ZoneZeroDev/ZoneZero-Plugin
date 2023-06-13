package kiinse.me.zonezero.plugin.config.enums

enum class ConfigKey(val value: String) {
    CREDENTIALS_TOKEN("token"),
    CREDENTIALS_SERVER_NAME("serverName"),
    DEFAULT_LOCALE("defaultLocale"),
    TOOLS_IS_DEBUG("debug"),
    TOOLS_CUSTOM_IP("customIp"),
    VERSION("version"),
    SETTINGS_ALLOW_CHAT("allowChat"),
    KICK_UNREGISTERED("kickNonRegistered"),
    KICK_WRONG_PASSWORD("kickOnWrongPassword"),
    SETTINGS_ALLOW_COMMANDS("allowCommands"),
    JOIN_MESSAGE_ON_AUTH("joinMessageOnAuth"),
    CUSTOM_JOIN_MESSAGE("customJoinMessage"),
    CUSTOM_LEAVE_MESSAGE("customLeaveMessage"),
    REMOVE_UNLOGGED_LEAVE_MESSAGE("removeUnloggedLeaveMessage"),
    REMOVE_JOIN_MESSAGE("removeJoinMessage"),
    REMOVE_LEAVE_MESSAGE("removeLeaveMessage"),
    APPLY_BLIND_EFFECT("applyBlindEffect"),
    SEND_TITLE("sendTitle"),
    TITLE_TIME("titleTime"),
    JOIN_DELAY("joinDelay")
}
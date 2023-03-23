package kiinse.me.oneconnect.plugin.enums

enum class Config(val value: String) {
    TABLE_CREDENTIALS("credentials"),
    TABLE_TOOLS("tools"),
    TABLE_CONFIG("config"),
    TABLE_SETTINGS("settings"),
    CREDENTIALS_TOKEN("token"),
    TOOLS_IS_DEBUG("debug"),
    CONFIG_VERSION("version"),
    SETTINGS_ALLOW_CHAT("allowChat"),
    KICK_UNREGISTERED("kickNonRegistered"),
    KICK_WRONG_PASSWORD("kickOnWrongPassword"),
    SETTINGS_ALLOW_COMMANDS("allowCommands"),
    CUSTOM_JOIN_MESSAGE("customJoinMessage"),
    CUSTOM_LEAVE_MESSAGE("customLeaveMessage"),
    REMOVE_UNLOGGED_LEAVE_MESSAGE("removeUnloggedLeaveMessage"),
    REMOVE_JOIN_MESSAGE("removeJoinMessage"),
    REMOVE_LEAVE_MESSAGE("removeLeaveMessage"),
    APPLY_BLIND_EFFECT("applyBlindEffect")
}
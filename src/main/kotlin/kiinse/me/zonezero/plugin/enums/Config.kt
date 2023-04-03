package kiinse.me.zonezero.plugin.enums

enum class Config(val value: String) {
    TABLE_CREDENTIALS("credentials"),
    TABLE_TOOLS("tools"),
    TABLE_CONFIG("config"),
    TABLE_SETTINGS("settings"),
    TABLE_MESSAGES("messages"),
    CREDENTIALS_TOKEN("token"),
    TOOLS_IS_DEBUG("debug"),
    TOOLS_CUSTOM_IP("customIp"),
    CONFIG_VERSION("version"),
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
    APPLY_BLIND_EFFECT("applyBlindEffect")
}
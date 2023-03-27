package kiinse.me.zonezero.plugin.commands.enums

@Suppress("UNUSED")
enum class CommandFailReason(val key: String) {
    INSUFFICIENT_PARAMETER("insufficient_parameter"),
    REDUNDANT_PARAMETER("redundant_parameter"),
    NO_PERMISSION("no_permission"),
    NOT_PLAYER("not_player"),
    COMMAND_NOT_FOUND("command_not_found"),
    REFLECTION_ERROR("reflection_error")
}
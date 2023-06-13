package kiinse.me.zonezero.plugin.enums

enum class SubTitle(val key: String) {
    WELCOME("subtitle_welcome"),
    LOGIN("subtitle_login"),
    REGISTER("subtitle_register"),
    REGISTER_SUCCESS("subtitle_register_success"),
    ERROR("subtitle_error"),
    TWO_FA_SEND("subtitle_2fa_send"),
    TWO_FA_INCORRECT("subtitle_2fa_incorrect"),
    TWO_FA_EXPIRED("subtitle_2fa_expired"),
    TWO_FA_ENABLED("subtitle_2fa_enabled"),
    TWO_FA_DISABLED("subtitle_2fa_disabled"),
    PASSWORD_WRONG("subtitle_password_wrong"),
    PASSWORD_UNSAFE("subtitle_password_unsafe"),
    PASSWORD_MISMATCH("subtitle_password_mismatch"),
    EMAIL_MISMATCH("subtitle_email_mismatch"),
    PASSWORD_CHANGED("subtitle_password_changed"),
    EMPTY("")
}
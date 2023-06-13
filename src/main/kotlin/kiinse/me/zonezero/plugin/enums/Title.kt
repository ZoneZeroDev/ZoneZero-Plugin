package kiinse.me.zonezero.plugin.enums

enum class Title(val key: String) {
    WELCOME("title_welcome"),
    LOGIN("title_login"),
    REGISTER("title_register"),
    REGISTER_SUCCESS("title_register_success"),
    ERROR("title_error"),
    TWO_FA_SEND("title_2fa_send"),
    TWO_FA_ERROR("title_2fa_error"),
    TWO_FA_SUCCESS("title_2fa_success"),
    PASSWORD_WRONG("title_password_wrong"),
    PASSWORD_UNSAFE("title_password_unsafe"),
    PASSWORD_MISMATCH("title_password_mismatch"),
    EMAIL_MISMATCH("title_email_mismatch"),
    PASSWORD_CHANGED("title_password_changed"),
    EMPTY("")
}
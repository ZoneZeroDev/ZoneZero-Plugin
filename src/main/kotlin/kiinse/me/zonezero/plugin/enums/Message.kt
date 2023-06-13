package kiinse.me.zonezero.plugin.enums

enum class Message(val key: String) {
    WELCOME_MESSAGE("welcome_message"),
    HELP_MESSAGE("help_message"),
    PLEASE_REGISTER("please_register"),
    PLEASE_LOGIN("please_login"),
    ALREADY_REGISTERED("already_registered"),
    NOT_REGISTERED("not_registered"),
    PLUGIN_RESTARTED("plugin_restarted"),
    ERROR_ON_RESTART("error_on_restart"),
    ALREADY_LOGGED_IN("already_logged_in"),
    SUCCESSFULLY_LOGGED_IN("successfully_logged_in"),
    SUCCESSFULLY_LOGGED_IN_IP("successfully_logged_in_ip"),
    SUCCESSFULLY_REGISTERED("successfully_registered"),
    SUCCESSFULLY_PASSWORD_CHANGED("successfully_password_changed"),
    SUCCESSFULLY_TWO_FACTOR_ENABLED("successfully_two_factor_enabled"),
    SUCCESSFULLY_TWO_FACTOR_DISABLED("successfully_two_factor_disabled"),
    SUCCESSFULLY_ACCOUNT_REMOVED("successfully_account_removed"),
    TWO_FACTOR_ALREADY_ENABLED("two_factor_already_enabled"),
    TWO_FACTOR_ALREADY_DISABLED("two_factor_already_disabled"),
    TWO_FACTOR_INCORRECT("two_factor_incorrect"),
    WRONG_PASSWORD("wrong_password"),
    PASSWORD_MISMATCH("password_mismatch"),
    EMAIL_MISMATCH("email_mismatch"),
    ERROR_ON_REGISTER("error_on_register"),
    ERROR_ON_LOGIN("error_on_login"),
    ERROR_ON_PASSWORD_CHANGE("error_on_password_change"),
    ERROR_ON_TWO_FACTOR("error_on_two_factor"),
    ERROR_ON_REMOVE("error_on_remove"),
    PLEASE_WAIT("please_wait"),
    TOO_MANY_ATTEMPTS("too_many_attempts"),
    AUTHORIZE_ON_SERVER("authorize_on_server"),
    KICK_UNREGISTERED("kick_unregistered"),
    TWO_FACTOR_SENT("two_factor_sent"),
    TWO_FACTOR_HELP("two_factor_help"),
    NOT_EMAIL("not_email"),
    WRONG_PASSWORD_SIZE("wrong_password_size"),
    CODE_OUTDATED("code_outdated")
}
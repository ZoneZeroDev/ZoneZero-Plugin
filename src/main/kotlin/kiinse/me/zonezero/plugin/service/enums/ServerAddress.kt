package kiinse.me.zonezero.plugin.service.enums

enum class ServerAddress(val string: String) {
    IS_PLUGIN_REGISTERED("server/isRegistered"),
    TEST_TOKEN("server/testToken"),
    GET_CODE("server/getCode"),
    IS_SERVER_ALLOWED("server/isServerAllowed"),
    LOGIN_PLAYER("players/login/standard"),
    LOGIN_PLAYER_BY_IP("players/login/ip"),
    REGISTER_PLAYER("players/register"),
    CHANGE_PASSWORD("players/changePassword"),
    TWO_FA_ENABLE("players/2fa/enable"),
    TWO_FA_DISABLE("players/2fa/disable"),
    TWO_FA_CODE("players/2fa/code")
}
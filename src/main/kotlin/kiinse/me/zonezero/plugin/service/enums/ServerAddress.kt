package kiinse.me.zonezero.plugin.service.enums

enum class ServerAddress(val string: String) {
    GET_CODE("server/getCode"),
    LOGIN_PLAYER("players/login/standard"),
    LOGIN_PLAYER_BY_IP("players/login/ip"),
    REMOVE_PLAYER("players/remove"),
    REGISTER_PLAYER("players/register"),
    CHANGE_PASSWORD("players/changePassword"),
    TWO_FA_ENABLE("players/2fa/enable"),
    TWO_FA_DISABLE("players/2fa/disable"),
    TWO_FA_CODE("players/2fa/code"),
    TEST_GET("test/getTestMessageGet"),
    TEST_POST("test/getTestMessagePost")
}
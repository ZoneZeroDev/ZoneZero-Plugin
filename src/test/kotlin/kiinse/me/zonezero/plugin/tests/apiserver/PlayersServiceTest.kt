package kiinse.me.zonezero.plugin.tests.apiserver

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.PlayersService
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.config.enums.ConfigTable
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.service.ApiConnection
import kiinse.me.zonezero.plugin.service.body.*
import kiinse.me.zonezero.plugin.utils.FilesUtils
import kotlin.test.*

class PlayersServiceTest {

    private var server: ServerMock? = null
    private var playersData: PlayersData? = null

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        val zonezero = MockBukkit.load(ZoneZero::class.java)
        val filesUtils = FilesUtils(zonezero)
        val tomlFile = filesUtils.getTomlFile(Strings.CONFIG_FILE.value)
        val apiService = ApiConnection(zonezero, tomlFile.getTable(ConfigTable.TOOLS))
        apiService.updateServerKey()
        playersData = PlayersService(zonezero, apiService, tomlFile.getTable(ConfigTable.SETTINGS))
    }

    @Test
    fun authPlayerStandardTest() {
        print("Testing standard auth (ok)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.authPlayer(player, PlayerLoginBody("test", playersData!!.getPlayerIp(player))) {
            assertEquals(200, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun authPlayerEmailTest() {
        print("Testing email auth (2fa)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.authPlayer(player, PlayerLoginBody("test", playersData!!.getPlayerIp(player))) {
            assertEquals(202, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun authPlayerIpTest() {
        print("Testing ip auth (ok)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.authPlayerByIp(player) {
            assertNotEquals(200, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun passwordChangeTest() {
        print("Testing password change (2fa)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.changePassword(player, PlayerPasswordChangeBody("test", "hello_world")) {
            assertEquals(202, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun passwordChangeWrongPasswordTest() {
        print("Testing change password (wrong password)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.changePassword(player, PlayerPasswordChangeBody("hello_world", "hello_world")) {
            assertEquals(401, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun passwordChangeNotRegisteredTest() {
        print("Testing change password (not registered)...")
        val player = server!!.addPlayer("test_sdfsdfsdfsdfsdfsdfsdf")
        playersData!!.changePassword(player, PlayerPasswordChangeBody("hello_world", "hello_world")) {
            assertEquals(404, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun passwordChangeWrongSizeTest() {
        print("Testing change password (wrong password size)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.changePassword(player, PlayerPasswordChangeBody("test", "hey")) {
            assertEquals(406, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun registerWrongSizeTest() {
        print("Testing register (wrong password size)...")
        val player = server!!.addPlayer("test_dgfdfgsdfsdfsdfsdf")
        playersData!!.registerPlayer(player, PlayerRegisterBody("hey")) {
            assertEquals(406, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun passwordMismatchTest() {
        print("Testing auth (wrong password)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.authPlayer(player, PlayerLoginBody("hello_world", playersData!!.getPlayerIp(player))) {
            assertEquals(401, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun alreadyRegisteredTest() {
        print("Testing register (already registered)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.registerPlayer(player, PlayerRegisterBody("hello_world")) {
            assertEquals(403, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun notRegisteredTest() {
        print("Testing auth (not registered)...")
        val player = server!!.addPlayer("test_dgdfgasedfsdgwsfdfsdfgsdg")
        playersData!!.authPlayer(player, PlayerLoginBody("hello_world", playersData!!.getPlayerIp(player))) {
            assertEquals(404, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun registerTest() {
        print("Testing register (ok)...")
        val player = server!!.addPlayer("test_register")
        playersData!!.removePlayer(player, PlayerRemoveBody("hello_world")) {}.start()
        playersData!!.registerPlayer(player, PlayerRegisterBody("hello_world")) {
            assertEquals(200, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun removePlayerTest() {
        print("Testing player remove (ok)...")
        val player = server!!.addPlayer("test_register")
        playersData!!.registerPlayer(player, PlayerRegisterBody("hello_world")) {}.start()
        playersData!!.removePlayer(player, PlayerRemoveBody("hello_world")) {
            assertEquals(200, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun playerStatusChangeTest() {
        print("Testing status change (ok)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
        assertEquals(PlayerStatus.NOT_AUTHORIZED, playersData!!.getPlayerStatus(player))
        playersData!!.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
        assertEquals(PlayerStatus.AUTHORIZED, playersData!!.getPlayerStatus(player))
        print(" OK\n")
    }

    @Test
    fun enableTwoFaTest() {
        print("Testing 2fa enable (ok)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.enableTwoFa(player, PlayerTwoFaEnableBody("test", "test@kiinse.me")) {
            assertEquals(200, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun enableTwoFaWrongPasswordTest() {
        print("Testing 2fa enable (wrong password)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.enableTwoFa(player, PlayerTwoFaEnableBody("hello_world", "test@kiinse.me")) {
            assertEquals(401, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun enableTwoFaAlreadyTest() {
        print("Testing 2fa enable (already enabled)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.enableTwoFa(player, PlayerTwoFaEnableBody("test", "test@kiinse.me")) {
            assertEquals(406, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun enableTwoFaNotRegisteredTest() {
        print("Testing 2fa enable (not registered)...")
        val player = server!!.addPlayer("test_dfgdfgdfgdfgdfgdfgdfg")
        playersData!!.enableTwoFa(player, PlayerTwoFaEnableBody("test", "test@kiinse.me")) {
            assertEquals(404, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun enableTwoFaBotEmailTest() {
        print("Testing 2fa enable (not email)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.enableTwoFa(player, PlayerTwoFaEnableBody("test", "testkiinse")) {
            assertEquals(405, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun disableTwoFaTest() {
        print("Testing 2fa disable (ok)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.disableTwoFa(player, PlayerTwoFaDisableBody("test")) {
            assertEquals(200, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun disableTwoFaWrongPasswordTest() {
        print("Testing 2fa disable (wrong password)...")
        val player = server!!.addPlayer("test_email")
        playersData!!.disableTwoFa(player, PlayerTwoFaDisableBody("hello_world")) {
            assertEquals(401, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun disableTwoFaAlreadyTest() {
        print("Testing 2fa disable (already disabled)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.disableTwoFa(player, PlayerTwoFaDisableBody("test")) {
            assertEquals(406, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun disableTwoFaNotRegisteredTest() {
        print("Testing 2fa disable (not registered)...")
        val player = server!!.addPlayer("test_dfgdfgdfgdfgdfgdfgdfg")
        playersData!!.disableTwoFa(player, PlayerTwoFaDisableBody("test")) {
            assertEquals(403, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun twoFaCodeIncorrectTest() {
        print("Testing 2fa code (incorrect)...")
        val player = server!!.addPlayer("test_standard")
        playersData!!.codeTwoFa(player, PlayerTwoFaCodeBody("hello_world")) {
            assertEquals(406, it.status)
        }.start()
        print(" OK\n")
    }

    @Test
    fun twoFaCodeNotRegisteredTest() {
        print("Testing 2fa code (not registered)...")
        val player = server!!.addPlayer("test_dfgdfgdfgdfgdfgdfgdfg")
        playersData!!.codeTwoFa(player, PlayerTwoFaCodeBody("hello_world")) {
            assertEquals(404, it.status)
        }.start()
        print(" OK\n")
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

}
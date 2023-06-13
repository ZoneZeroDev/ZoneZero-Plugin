package kiinse.me.zonezero.plugin.tests.service

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.PlayersService
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.config.enums.ConfigTable
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.service.ApiConnection
import kiinse.me.zonezero.plugin.service.body.TestPostBody
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.post
import kiinse.me.zonezero.plugin.utils.FilesUtils
import kotlin.test.*


class ApiConnectionTest {

    private var server: ServerMock? = null
    private var apiService: ApiConnection? = null
    private var playersData: PlayersData? = null

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        val zonezero = MockBukkit.load(ZoneZero::class.java)
        val filesUtils = FilesUtils(zonezero)
        val tomlFile = filesUtils.getTomlFile(Strings.CONFIG_FILE.value)
        apiService = ApiConnection(zonezero, tomlFile.getTable(ConfigTable.TOOLS))
        apiService!!.updateServerKey()
        playersData = PlayersService(zonezero, apiService!!, tomlFile.getTable(ConfigTable.SETTINGS))
    }

    @Test
    fun getTest() {
        print("Testing GET decryption...")
        val answer = apiService!!.get(ServerAddress.TEST_GET)
        assertEquals(200, answer.status)
        assertTrue { return@assertTrue (answer.getMessage() == "Hello World!") }
        print(" OK\n")
    }

    @Test
    fun postTest() {
        print("Testing POST decryption and encryption...")
        val answer = apiService!!.post<TestPostBody>(ServerAddress.TEST_POST, TestPostBody("i love you"))
        assertEquals(200, answer.status)
        assertTrue { return@assertTrue (answer.getMessage() == "Hello World!") }
        assertEquals(406, apiService!!.post<TestPostBody>(ServerAddress.TEST_POST, TestPostBody()).status)
        print(" OK\n")
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

}
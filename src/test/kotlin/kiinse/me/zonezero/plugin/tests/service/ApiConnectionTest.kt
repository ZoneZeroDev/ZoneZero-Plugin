package kiinse.me.zonezero.plugin.tests.service

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.PlayersService
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.service.ApiConnection
import kiinse.me.zonezero.plugin.service.body.TestPostBody
import kiinse.me.zonezero.plugin.service.enums.ServerAddress
import kiinse.me.zonezero.plugin.service.interfaces.post
import kiinse.me.zonezero.plugin.utils.FilesUtils
import kotlin.test.*


class ApiConnectionTest {

    private var server: ServerMock? = null
    private var zonezero: ZoneZero? = null
    private var apiService: ApiConnection? = null
    private var playersData: PlayersData? = null

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        zonezero = MockBukkit.load(ZoneZero::class.java)
        val filesUtils = FilesUtils(zonezero!!)
        val configuration = filesUtils.getTomlFile(Strings.CONFIG_FILE.value)
        val toolsTable = configuration.getTableOrEmpty(Config.TABLE_TOOLS.value)
        val settingsTable = configuration.getTableOrEmpty(Config.TABLE_SETTINGS.value)
        apiService = ApiConnection(zonezero!!, toolsTable)
        apiService!!.updateServerKey()
        playersData = PlayersService(zonezero!!, apiService!!, settingsTable);
    }

    @Test
    fun getTest() {
        print("Testing GET decryption...")
        val answer = apiService!!.get(ServerAddress.TEST_GET)
        assertEquals(200, answer.status)
        assertTrue { return@assertTrue (answer.getMessageAnswer().message == "Hello World!") }
        print(" OK\n")
    }

    @Test
    fun postTest() {
        print("Testing POST decryption and encryption...")
        val answer = apiService!!.post<TestPostBody>(ServerAddress.TEST_POST, TestPostBody("i love you"))
        assertEquals(200, answer.status)
        assertTrue { return@assertTrue (answer.getMessageAnswer().message == "Hello World!") }
        assertEquals(406, apiService!!.post<TestPostBody>(ServerAddress.TEST_POST, TestPostBody()).status)
        print(" OK\n")
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

}
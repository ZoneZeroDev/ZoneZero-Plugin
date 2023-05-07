package kiinse.me.zonezero.plugin.tests.listeners

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.PlayersService
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.commands.core.CommandManager
import kiinse.me.zonezero.plugin.commands.zonezero.LoginCommand
import kiinse.me.zonezero.plugin.commands.zonezero.RegisterCommand
import kiinse.me.zonezero.plugin.commands.zonezero.ZoneZeroCommand
import kiinse.me.zonezero.plugin.enums.Config
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.listeners.*
import kiinse.me.zonezero.plugin.service.ApiConnection
import kiinse.me.zonezero.plugin.utils.FilesUtils
import org.bukkit.Location
import org.bukkit.Material
import org.tomlj.TomlTable
import kotlin.test.*

class ListenersTest {

    private var server: ServerMock? = null
    private var zonezero: ZoneZero? = null
    private var apiService: ApiConnection? = null
    private var playersData: PlayersData? = null
    private var settingsTable: TomlTable? = null

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        zonezero = MockBukkit.load(ZoneZero::class.java)
        val filesUtils = FilesUtils(zonezero!!)
        val configuration = filesUtils.getTomlFile(Strings.CONFIG_FILE.value)
        val toolsTable = configuration.getTableOrEmpty(Config.TABLE_TOOLS.value)
        settingsTable = configuration.getTableOrEmpty(Config.TABLE_SETTINGS.value)
        apiService = ApiConnection(zonezero!!, toolsTable)
        apiService!!.updateServerKey()
        playersData = PlayersService(zonezero!!, apiService!!, settingsTable!!);
    }

    @Test
    fun moveTest() {
        print("Testing player move listener...")
        server!!.pluginManager.registerEvents(MoveListener(playersData!!), zonezero!!)
        val player = server!!.addPlayer("test_standard")
        val location = player.location

        playersData!!.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
        val notMove = player.simulatePlayerMove(Location(player.world, location.x + 100, location.y, location.z))
        assertEquals(true, notMove.isCancelled)

        playersData!!.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
        val move = player.simulatePlayerMove(Location(player.world, location.x + 100, location.y, location.z))
        assertEquals(false, move.isCancelled)
        print(" OK\n")
    }

    @Test
    fun miningTest() {
        print("Testing player mining listener...")
        server!!.pluginManager.registerEvents(MiningListener(playersData!!), zonezero!!)
        val player = server!!.addPlayer("test_standard")
        val location = player.location

        playersData!!.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
        val notMining = player.simulateBlockBreak(location.block)
        assertEquals(true, notMining!!.isCancelled)

        playersData!!.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
        val mining = player.simulateBlockBreak(location.block)
        assertEquals(false, mining!!.isCancelled)
        print(" OK\n")
    }

    @Test
    fun damageTest() {
        print("Testing player damage listener...")
        server!!.pluginManager.registerEvents(DamageListener(playersData!!), zonezero!!)
        val player = server!!.addPlayer("test_standard")

        playersData!!.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
        val playerHealthBefore = player.health
        player.damage(10.0)
        assertEquals(playerHealthBefore, player.health)

        playersData!!.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
        player.damage(10.0)
        assertNotEquals(playerHealthBefore, player.health)
        print(" OK\n")
    }

    @Test
    fun blockPlaceListener() {
        print("Testing player block place listener...")
        server!!.pluginManager.registerEvents(BlockPlaceListener(playersData!!), zonezero!!)
        val player = server!!.addPlayer("test_standard")
        val location = player.location

        playersData!!.setPlayerStatus(player, PlayerStatus.NOT_AUTHORIZED)
        val notPlace = player.simulateBlockPlace(Material.STONE, Location(location.world, location.x, location.y + 3, location.x))
        assertEquals(true, notPlace!!.isCancelled)

        playersData!!.setPlayerStatus(player, PlayerStatus.AUTHORIZED)
        val place = player.simulateBlockPlace(Material.STONE, Location(location.world, location.x, location.y + 3, location.x))
        assertEquals(false, place!!.isCancelled)
        print(" OK\n")
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

}
package kiinse.me.zonezero.plugin.tests.listeners

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.apiserver.PlayersService
import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.apiserver.interfaces.PlayersData
import kiinse.me.zonezero.plugin.config.enums.ConfigTable
import kiinse.me.zonezero.plugin.enums.Strings
import kiinse.me.zonezero.plugin.listeners.BlockPlaceListener
import kiinse.me.zonezero.plugin.listeners.DamageListener
import kiinse.me.zonezero.plugin.listeners.MiningListener
import kiinse.me.zonezero.plugin.listeners.MoveListener
import kiinse.me.zonezero.plugin.service.ApiConnection
import kiinse.me.zonezero.plugin.utils.FilesUtils
import org.bukkit.Location
import org.bukkit.Material
import kotlin.test.*

class ListenersTest {

    private var server: ServerMock? = null
    private var zonezero: ZoneZero? = null
    private var playersData: PlayersData? = null

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        zonezero = MockBukkit.load(ZoneZero::class.java)
        val filesUtils = FilesUtils(zonezero!!)
        val tomlFile = filesUtils.getTomlFile(Strings.CONFIG_FILE.value)
        val apiService = ApiConnection(zonezero!!, tomlFile.getTable(ConfigTable.TOOLS))
        apiService.updateServerKey()
        playersData = PlayersService(zonezero!!, apiService, tomlFile.getTable(ConfigTable.SETTINGS))
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
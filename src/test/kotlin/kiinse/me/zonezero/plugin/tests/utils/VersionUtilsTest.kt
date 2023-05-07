package kiinse.me.zonezero.plugin.tests.utils

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kiinse.me.zonezero.plugin.ZoneZero
import kiinse.me.zonezero.plugin.utils.VersionUtils
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionUtilsTest {

    private var server: ServerMock? = null
    private var zonezero: ZoneZero? = null

    @BeforeTest
    fun setUp() {
        server = MockBukkit.mock()
        zonezero = MockBukkit.load(ZoneZero::class.java)
    }

    @Test
    fun versionCheckTest() {
        print("Testing check version...")
        VersionUtils.getLatestSpigotVersion {
            assertEquals(true, VersionUtils.getPluginVersion(zonezero!!).isGreaterThanOrEqualTo(it))
        }
        print(" OK\n")
    }

    @AfterTest
    fun tearDown() {
        MockBukkit.unmock()
    }

}
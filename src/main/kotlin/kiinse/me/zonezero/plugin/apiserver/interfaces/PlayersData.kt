package kiinse.me.zonezero.plugin.apiserver.interfaces

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.service.body.*
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kotlinx.coroutines.Deferred
import org.bukkit.entity.Player
import java.util.function.Consumer

interface PlayersData {

    fun getPlayerStatus(player: Player): PlayerStatus
    fun setPlayerStatus(player: Player, status: PlayerStatus)
    fun addJoinMessage(player: Player, string: String)
    fun removeJoinMessage(player: Player)
    fun authPlayer(player: Player, body: PlayerLoginBody, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun removePlayer(player: Player, body: PlayerRemoveBody, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun changePassword(player: Player, body: PlayerPasswordChangeBody, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun authPlayerByIp(player: Player, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun registerPlayer(player: Player, body: PlayerRegisterBody, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun enableTwoFa(player: Player, body: PlayerTwoFaEnableBody, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun disableTwoFa(player: Player, body: PlayerTwoFaDisableBody, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun codeTwoFa(player: Player, body: PlayerTwoFaCodeBody, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun getPlayerIp(player: Player): String
    fun savePlayersStatuses()

}
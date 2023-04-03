package kiinse.me.zonezero.plugin.apiserver.interfaces

import kiinse.me.zonezero.plugin.apiserver.enums.PlayerStatus
import kiinse.me.zonezero.plugin.service.data.ServerAnswer
import kotlinx.coroutines.Deferred
import org.bukkit.entity.Player
import java.util.function.Consumer

interface PlayersData {

    fun getPlayerStatus(player: Player): PlayerStatus
    fun setPlayerStatus(player: Player, status: PlayerStatus)
    fun addJoinMessage(player: Player, string: String)
    fun removeJoinMessage(player: Player)
    fun authPlayer(player: Player, password: String, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun changePassword(player: Player, oldPassword: String, newPassword: String, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun authPlayerByIp(player: Player, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun registerPlayer(player: Player, password: String, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun enableTwoFa(player: Player, email: String, password: String, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun disableTwoFa(player: Player, password: String, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun codeTwoFa(player: Player, code: String, consumer: Consumer<ServerAnswer>): Deferred<Unit>
    fun savePlayersStatuses()

}
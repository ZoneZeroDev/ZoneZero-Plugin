package kiinse.me.oneconnect.plugin.apiserver.interfaces

import kiinse.me.oneconnect.plugin.apiserver.enums.PlayerStatus
import kiinse.me.oneconnect.plugin.service.ServerAnswer
import org.bukkit.entity.Player
import javax.xml.stream.Location

interface PlayersData {

    fun getPlayerStatus(player: Player): PlayerStatus
    fun setPlayerStatus(player: Player, status: PlayerStatus)
    fun authPlayer(player: Player, password: String): ServerAnswer
    fun changePassword(player: Player, oldPassword: String, newPassword: String): ServerAnswer
    fun authPlayerByIp(player: Player): ServerAnswer
    fun registerPlayer(player: Player, password: String): ServerAnswer
    fun enableTwoFa(player: Player, email: String, password: String): ServerAnswer
    fun disableTwoFa(player: Player, password: String): ServerAnswer
    fun codeTwoFa(player: Player, code: String): ServerAnswer
    fun savePlayersStatuses()

}
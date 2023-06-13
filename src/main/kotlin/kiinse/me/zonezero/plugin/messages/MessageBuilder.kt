package kiinse.me.zonezero.plugin.messages

import kiinse.me.zonezero.plugin.enums.Message
import kiinse.me.zonezero.plugin.enums.SubTitle
import kiinse.me.zonezero.plugin.enums.Title
import kiinse.me.zonezero.plugin.enums.TitleType
import kiinse.me.zonezero.plugin.utils.MessageUtils
import org.bukkit.entity.Player

class MessageBuilder(private val messageUtils: MessageUtils, private val player: Player) {

    private var message: Message? = null
    private var replaceMap: HashMap<String, String> = hashMapOf()
    private var title: Title? = null
    private var subtitle: SubTitle = SubTitle.EMPTY
    private var titleType: TitleType = TitleType.DEFAULT
    private var titleTime: Int = messageUtils.titleTime

    fun setMessage(message: Message): MessageBuilder {
        this.message = message
        return this
    }

    fun setReplaceMap(replaceMap: HashMap<String, String>): MessageBuilder {
        this.replaceMap = replaceMap
        return this
    }

    fun setTitleType(titleType: TitleType): MessageBuilder {
        this.titleType = titleType
        return this
    }

    fun setTitle(title: Title): MessageBuilder {
        this.title = title
        return this
    }

    fun setSubTitle(subtitle: SubTitle): MessageBuilder {
        this.subtitle = subtitle
        return this
    }

    fun setTitleTime(titleTime: Int): MessageBuilder {
        this.titleTime = titleTime
        return this
    }

    fun send() {
        if (message != null) {
            messageUtils.sendMessageWithPrefix(player, message!!, replaceMap)
        }
        if (title != Title.EMPTY || subtitle != SubTitle.EMPTY) {
            if (titleType == TitleType.DEFAULT) {
                messageUtils.sendDisplayMessage(player, title!!, subtitle, titleTime)
            } else {
                messageUtils.sendDisplayMessageWithName(player, title!!, subtitle, titleTime)
            }
        }
    }
}
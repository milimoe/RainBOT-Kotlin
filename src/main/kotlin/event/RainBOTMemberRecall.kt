package org.milimoe.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.events.MemberMuteEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.events.isByBot
import net.mamoe.mirai.event.events.operatorOrBot
import net.mamoe.mirai.utils.info
import org.milimoe.RainBOT
import org.milimoe.data.RainData
import org.milimoe.repeats
import org.milimoe.whomute

object RainBOTMemberRecall {
    suspend fun load(event: MessageRecallEvent) {
        if (Bot.instances[0].id != RainData.BOTQQ) return
        if (repeats.contains(event.messageIds)) {
            repeats.remove(event.messageIds)
            RainBOT.logger.info { "${event.messageIds} 消息被撤回，BOT将不会再复读该消息" }
        }
    }
}
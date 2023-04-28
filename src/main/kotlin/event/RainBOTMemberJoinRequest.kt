package org.milimoe.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import org.milimoe.data.RainData

object RainBOTMemberJoinRequest {
    suspend fun load(event: MemberJoinRequestEvent) {
        val bot = Bot.instances[0]
        if (bot.id != RainData.BOTQQ) return
        if (event.fromId == RainData.Master && event.group?.getMember(bot.id)?.isOperator() == true) {
            event.accept()
        }
    }
}
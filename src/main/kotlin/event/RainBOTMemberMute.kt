package org.milimoe.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.events.MemberMuteEvent
import net.mamoe.mirai.event.events.isByBot
import net.mamoe.mirai.event.events.operatorOrBot
import org.milimoe.data.RainData
import org.milimoe.whomute

object RainBOTMemberMute {
    suspend fun load(event: MemberMuteEvent) {
        if (Bot.instances[0].id != RainData.BOTQQ) return
        if (!event.isByBot) {
            whomute[event.member.id] = event.operatorOrBot.id
            if (event.member.id == RainData.Master) {
                event.group.getMember(event.member.id)?.unmute()
                event.group.sendMessage("检测到主人被 ${event.operatorOrBot.nick}（${event.operatorOrBot.id}）禁言！")
                whomute[event.member.id] = RainData.Master
                event.operatorOrBot.mute(60)
            }
        } else {
            if (event.member.id == RainData.Master) {
                event.group.getMember(event.member.id)?.unmute()
            }
        }
    }
}
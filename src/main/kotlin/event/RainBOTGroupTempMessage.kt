package org.milimoe.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.milimoe.data.RainData
import org.milimoe.whomute

object RainBOTGroupTempMessage {
    suspend fun load(event: GroupTempMessageEvent) {
        if (RainData.IsRun != 1L) return
        if (Bot.instances[0].id != RainData.BOTQQ) return
        val messageChain: MessageChain = event.message
        val msg: String = messageChain.contentToString()
        if (msg == "忏悔") {
            val bot = Bot.instances[0]
            val sender = event.sender
            val groups: ContactList<Group> = bot.groups
            if (groups.isNotEmpty()) {
                var ischanhui = false
                for (g in groups) {
                    val m : NormalMember = g.getMember(sender.id) ?: return
                    if (whomute.containsKey(sender.id)) {
                        if (whomute[sender.id] != sender.id && m.isMuted) {
                            ischanhui = true
                            event.subject.sendMessage("[群${g.id}] 忏悔失败，你是被管理员禁言的，不能为你解禁。")
                        } else {
                            if (m.isMuted) {
                                ischanhui = true
                                m.unmute()
                                whomute.remove(sender.id)
                                event.subject.sendMessage("[群${g.id}] 忏悔成功！！希望你保持纯真，保持野性的美。")
                            }
                        }
                    } else {
                        if (m.isMuted) {
                            ischanhui = true
                            m.unmute()
                            whomute.remove(sender.id)
                            event.subject.sendMessage("[群${g.id}] 忏悔成功！！希望你保持纯真，保持野性的美。")
                        }
                    }
                }
                if (!ischanhui) event.subject.sendMessage("你无需忏悔。")
            }
        }
    }
}
package org.milimoe.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info
import org.milimoe.RainBOT
import org.milimoe.dailys
import org.milimoe.data.RainData
import org.milimoe.whomute

object RainBOTFriendMessage {
    suspend fun load(event: FriendMessageEvent) {
        if (RainData.IsRun != 1L) return
        if (Bot.instances[0].id != RainData.BOTQQ) return
        val messageChain: MessageChain = event.message
        val msg: String = messageChain.contentToString()
        if (event.sender.id == RainData.Master) {
            if (msg == "是") {
                event.subject.sendMessage("是你的头")
            }
            if (msg == "重置运势") {
                dailys.clear()
                RainBOT.logger.info { "每日运势已刷新" }
                event.subject.sendMessage("每日运势已刷新")
            }
            if (msg == "群发早安") {
                RainBOT.getNews()
            }
            if (msg == "群发晚安") {
                val bot = Bot.instances[0]
                val groups: ContactList<Group> = bot.groups
                val chain = messageChainOf(PlainText("Good Night～").plus("[mirai:face:75]".deserializeMiraiCode()).plus(PlainText("明天见")))
                for (g in groups) {
                    g.sendMessage(chain)
                }
            }
            if (msg.getLeftString(3) == "发送至") {
                val list = msg.split('\n')
                if (list.count() >= 2) {
                    var m = list[0].replace("发送至", "").trim()
                    if (m.getNumber() != "") {
                        val g = m.getNumber().toLong()
                        m = ""
                        for (i in 1 until list.count()) {
                            m = m.plus(list[i])
                            if (i != list.count() - 1) m = m.plus("\n")
                        }
                        val bot = Bot.instances[0]
                        bot.getGroup(g)?.sendMessage(m)
                    }
                } else {
                    event.subject.sendMessage("发送群消息命令无效\n请遵守格式：发送至+群号\n换一行写内容\n例如：发送至1234567\n这里是发送的内容")
                }
            }
        }
        if (msg.getLeftString(2) == "群发") {
            val list = msg.split('\n')
            if (list.count() >= 2) {
                var m = ""
                for (i in 1 until list.count()) {
                    m = m.plus(list[i])
                    if (i != list.count() - 1) m = m.plus("\n")
                }
                val bot = Bot.instances[0]
                val groups: ContactList<Group> = bot.groups
                for (g in groups) {
                    g.sendMessage(m)
                }
            } else {
                event.subject.sendMessage("群发命令无效\n请遵守格式：群发\n换一行写内容\n例如：群发\n这里是发送的内容")
            }
        }
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
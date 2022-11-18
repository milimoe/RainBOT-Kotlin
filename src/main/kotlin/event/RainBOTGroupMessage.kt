package org.milimoe.event

import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.operatorOrBot
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.milimoe.RainBOT
import org.milimoe.RainBOT.logger
import org.milimoe.data.RainData
import org.milimoe.data.OSMCore
import org.milimoe.whomute
import java.io.File
import java.lang.Character.UnicodeBlock
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext

object MiraiBOTGroupMessage {
    suspend fun load(
        coroutineContext: CoroutineContext,
        event: GroupMessageEvent,
        dailys: HashMap<Long, String>,
        dailylist: List<String>
    ) {
        if (RainData.IsRun != 1L) return
        if (Bot.instances[0].id != RainData.BOTQQ) return
        val messageChain: MessageChain = event.message
        val msg: String = messageChain.contentToString()
        val sender = event.sender
        val senderID = sender.id
        val subject = event.subject
        if (msg == "是") {
            if (senderID == RainData.Master) {
                logger.info { "是你的头" }
                subject.sendMessage("是你的头")
            } else if ((1..5).random() <= 2) subject.sendMessage("是你的头")
        }
        /**
         * OSM核心功能
         * 随机复读
         */
        if (RainData.IsRepeat == 1L)  {
            if ((1..100).random() <= RainData.PRepeat) {
                sender.nudge().sendTo(event.group)
                val wait = (RainData.RepeatDelay[0]..RainData.RepeatDelay[1]).random()
                var isIgnore = false
                for (m in RainData.RepeatIgnore) {
                    if (msg.indexOf(m) != -1) {
                        isIgnore = true
                        break;
                    }
                }
                if (!isIgnore) {
                    logger.info { "触发了复读 -> " + (wait / 1000).toInt() + "秒后复读：$msg" }
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            CoroutineScope(RainBOT.coroutineContext).launch {
                                subject.sendMessage(messageChain)
                            }
                        }
                    }, wait)
                }
            }
        }
        if (msg.trim().getLeftString(4).lowercase() == "loli" && msg.trim().getRightString(3).lowercase() == "r18") {
            whomute[senderID] = RainData.Master
            sender.mute(9999)
            subject.sendMessage("禁止通行！")
            messageChain.recall()
        }
        /**
         * OSM核心功能
         * 随机反驳不
         */
        if (RainData.IsSayNo == 1L) {
            if (msg.trim().length in 6..50 && msg[msg.length - 1] == '。' && senderID != RainData.BOTQQ) {
                val sb = StringBuilder()
                for (i in 0..msg.length - 2) {
                    val c = msg[msg.length - 2 - i]
                    if (c != '\n') sb.append(c)
                }
                sb.append('。')
                subject.sendMessage(sb.toString())
            }
            if ((1..100).random() <= RainData.PSayNo) {
                val m = msg.getSayNo()
                if (m != "") {
                    subject.sendMessage(m)
                }
            }
        }
        /**
         * OSM核心功能
         * 禁言抽奖
         */
        if (msg.getLeftString(4) == "禁言抽奖") {
            if (RainData.IsMute == 1L) {
                Thread {
                    CoroutineScope(RainBOT.coroutineContext).launch {
                        subject.sendMessage("2秒后开奖～如需要忏悔，请在开奖后3秒内发送忏悔，开奖前发送无效。")
                        Thread.sleep(2000)
                        if (senderID != RainData.Master) {
                            whomute[senderID] = senderID
                            val time = (RainData.MuteTime[0]..RainData.MuteTime[1]).random()
                            subject.sendMessage("开奖啦！禁言时长：" + (time / 60).toInt() + "分钟。\n你现在有3秒时间发送忏悔拒绝领奖！")
                            Thread.sleep(3000)
                            sender.mute(time.toInt())
                        } else {
                            subject.sendMessage("我不能禁言主人！")
                        }
                    }
                }.start()
            } else {
                subject.sendMessage("禁言抽奖暂未开启，请让我的主人开启它！")
            }
        }
        if (msg == "忏悔") {
            if (senderID != RainData.BOTQQ || senderID != RainData.Master) {
                Timer().schedule(object: TimerTask() {
                    override fun run() {
                        CoroutineScope(RainBOT.coroutineContext).launch {
                            if (whomute.containsKey(sender.id)) {
                                if (whomute[sender.id] != sender.id) {
                                    event.subject.sendMessage("忏悔失败，你是被管理员禁言的，不能为你解禁。")
                                } else {
                                    val m: NormalMember? = event.group.getMember(sender.id)
                                    if (m?.isMuted == true) {
                                        m.unmute()
                                        whomute.remove(sender.id)
                                        event.subject.sendMessage("忏悔成功！！希望你保持纯真，保持野性的美。")
                                    }
                                }
                            } else {
                                val m: NormalMember? = event.group.getMember(sender.id)
                                if (m?.isMuted == true) {
                                    m.unmute()
                                    whomute.remove(sender.id)
                                    event.subject.sendMessage("忏悔成功！！希望你保持纯真，保持野性的美。")
                                }
                            }
                        }
                    }
                }, 3500)
            } else {
                subject.sendMessage("你无需忏悔。")
            }
        }
        /**
         * OSM核心功能
         * 禁言/解禁所有人
         */
        if (senderID == RainData.Master || senderID == RainData.BOTQQ || RainData.MuteAccessGroup.contains(senderID)) {
            if (msg.getLeftString(5) == "禁言所有人") {
                val contacts: ContactList<NormalMember> = subject.members
                if (msg == "禁言所有人") {
                    val time = (RainData.MuteTime[0]..720).random()
                    for (c in contacts) {
                        if (c.id != RainData.Master) {
                            whomute[c.id] = RainData.Master
                            c.mute(time.toInt())
                        }
                    }
                    subject.sendMessage("Done")
                } else {
                    val newmsg = msg.replace("禁言所有人", "").trim()
                    if (newmsg.matches("-?\\d+(\\.\\d+)?".toRegex())) {
                        val time = newmsg.toInt()
                        for (c in contacts) {
                            if (c.id != RainData.Master) {
                                whomute[c.id] = RainData.Master
                                c.mute(time)
                            }
                        }
                        subject.sendMessage("Done")
                    }
                }
            } else if (msg.getLeftString(2) == "禁言") {
                var m = messageChain.serializeToMiraiCode()
                if (m.indexOf("[mirai:at:") == -1) {
                    subject.sendMessage("禁言命令格式不正确\n请遵守格式：禁言@1@2..@N [时长]")
                    return
                } else {
                    val o = "[mirai:at:"
                    m = m.replace("禁言", "").trim()
                    val count = m.countString(o)
                    if (count == 0) return
                    val list = ArrayList<Long>()
                    for (i in 0..count) {
                        m = m.replaceFirst(o, "").trim()
                        val id = m.getAtQQNumber()
                        if (id != "") {
                            list.add(id.toLong())
                            m = m.replaceFirst(list.last().toString() + "]", "").trim()
                        }
                    }
                    m = m.replaceFirst("]", "").trim()
                    val strtime = m.getNumber()
                    if (list.isNotEmpty()) {
                        if (strtime != "") {
                            for (c in list) {
                                if (c != RainData.Master) {
                                    whomute[c] = RainData.Master
                                    event.group.getMember(c)?.mute(strtime.toInt())
                                }
                            }
                        } else {
                            for (c in list) {
                                val time = (RainData.MuteTime[0]..720).random()
                                if (c != RainData.Master) {
                                    whomute[c] = RainData.Master
                                    event.group.getMember(c)?.mute(time.toInt())
                                }
                            }
                        }
                    } else {
                        subject.sendMessage("禁言命令格式不正确\n请遵守格式：禁言@1@2..@N [时长]")
                    }
                }
            }
        }
        if (senderID == RainData.Master || senderID == RainData.BOTQQ || RainData.UnmuteAccessGroup.contains(senderID)) {
            if (msg.getLeftString(5) == "解禁所有人") {
                whomute.clear()
                logger.info { "已清空禁言成员表" }
                val contacts: ContactList<NormalMember> = subject.members
                for (c in contacts) {
                    if (c.id != RainData.Master) {
                        c.unmute()
                    }
                }
                subject.sendMessage("Done")
            } else if (msg.getLeftString(2) == "解禁") {
                var m = messageChain.serializeToMiraiCode()
                if (m.indexOf("[mirai:at:") == -1) {
                    subject.sendMessage("解禁命令格式不正确\n请遵守格式：解禁@1@2..@N")
                    return
                } else {
                    val o = "[mirai:at:"
                    m = m.replace("解禁", "").trim()
                    val count = m.countString(o)
                    if (count == 0) return
                    val list = ArrayList<Long>()
                    for (i in 0..count) {
                        m = m.replaceFirst(o, "").trim()
                        val id = m.getAtQQNumber()
                        if (id != "") {
                            list.add(id.toLong())
                            m = m.replaceFirst(list.last().toString() + "]", "").trim()
                        }
                    }
                    if (list.isNotEmpty()) {
                        for (c in list) {
                            if (c != RainData.Master) {
                                whomute.remove(c)
                                event.group.getMember(c)?.unmute()
                            }
                        }
                    } else {
                        subject.sendMessage("解禁命令格式不正确\n请遵守格式：解禁@1@2..@N")
                    }
                }
            }
        }
        /**
         * OSM核心功能
         * 设置/取消管理员
         */
        if (senderID == RainData.Master) {
            if (msg.getLeftString(9) == ".osm -put") {
                var m = messageChain.serializeToMiraiCode()
                if (m.indexOf("[mirai:at:") == -1) {
                    subject.sendMessage("命令格式不正确\n请遵守格式：.osm -put@1@2..@N")
                    return
                } else {
                    val o = "[mirai:at:"
                    m = m.replace(".osm -put", "").trim()
                    val count = m.countString(o)
                    if (count == 0) return
                    val list = ArrayList<Long>()
                    for (i in 0..count) {
                        m = m.replaceFirst(o, "").trim()
                        val id = m.getAtQQNumber()
                        if (id != "") {
                            list.add(id.toLong())
                            m = m.replaceFirst(list.last().toString() + "]", "").trim()
                        }
                    }
                    if (list.isNotEmpty()) {
                        for (c in list) {
                            whomute.remove(c)
                            event.group.getMember(c)?.modifyAdmin(true)
                        }
                    } else {
                        subject.sendMessage("命令格式不正确\n请遵守格式：.osm -put@1@2..@N")
                    }
                }
            }
            if (msg.getLeftString(12) == ".osm -remove") {
                var m = messageChain.serializeToMiraiCode()
                if (m.indexOf("[mirai:at:") == -1) {
                    subject.sendMessage("命令格式不正确\n请遵守格式：.osm -remove@1@2..@N")
                    return
                } else {
                    val o = "[mirai:at:"
                    m = m.replace(".osm -remove", "").trim()
                    val count = m.countString(o)
                    if (count == 0) return
                    val list = ArrayList<Long>()
                    for (i in 0..count) {
                        m = m.replaceFirst(o, "").trim()
                        val id = m.getAtQQNumber()
                        if (id != "") {
                            list.add(id.toLong())
                            m = m.replaceFirst(list.last().toString() + "]", "").trim()
                        }
                    }
                    if (list.isNotEmpty()) {
                        for (c in list) {
                            whomute.remove(c)
                            event.group.getMember(c)?.modifyAdmin(false)
                        }
                    } else {
                        subject.sendMessage("命令格式不正确\n请遵守格式：.osm -remove@1@2..@N")
                    }
                }
            }
        }
        /**
         * OSM核心功能
         * 随机OSM
         */
        if (RainData.IsOSM == 1L) {
            if ((1..100).random() <= RainData.POSM) {
                val img = File(RainData.GeneralPath).resolve("osm.png").uploadAsImage(subject)
                subject.sendMessage(img)
                return
            }
            // 随机答复 是呀
            val image: Image? by messageChain.orNull()
            if (image != null) {
               if ((1..100).random() <= RainData.POSM + RainData.PRepeat + RainData.PSayNo) {
                    val img = image!!.imageId
                    val png = File(RainData.GeneralPath).resolve("osm.png").uploadAsImage(subject).imageId
                    val gif = File(RainData.GeneralPath).resolve("osm.gif").uploadAsImage(subject).imageId
                    val jpg = File(RainData.GeneralPath).resolve("osm.jpg").uploadAsImage(subject).imageId
                    if (img == png || img == gif || img == jpg) {
                        val newimg = File(RainData.ShidePath).resolve("sd7.gif").uploadAsImage(subject)
                        subject.sendMessage(newimg)
                    }
                }
            }
        }
        /**
         * OSM基本功能组
         */
        if (msg.indexOf("来图") != -1) {
            getImg(coroutineContext, event, URL("https://iw233.cn/api.php?sort=random"))
        } else if (msg.indexOf("白毛") != -1) {
            getImg(coroutineContext, event, URL("https://iw233.cn/api.php?sort=yin"))
        } else if (msg == "猫耳") {
            getImg(coroutineContext, event, URL("https://iw233.cn/api.php?sort=cat"))
        } else if (msg == "壁纸") {
            getImg(coroutineContext, event, URL("https://iw233.cn/api.php?sort=pc"))
        } else if (msg == "一眼丁真") {
            val count = (1..RainData.Dingzhen).random()
            val img = File(RainData.DingzhenPath).resolve("dz$count.jpg").uploadAsImage(subject)
            subject.sendMessage(img)
        } else if (msg.indexOf("来龙") != -1 || msg.indexOf("龙图") != -1) {
            val count = (1..RainData.Longtu).random()
            val img = File(RainData.LongtuPath).resolve("long ($count).jpg").uploadAsImage(subject)
            subject.sendMessage(img)
        } else if (msg.indexOf("是吗") != -1) {
            val img = File(RainData.GeneralPath).resolve("osm.png").uploadAsImage(subject)
            subject.sendMessage(img)
        } else if (msg.indexOf("谔谔") != -1) {
            val img = File(RainData.GeneralPath).resolve("ee.png").uploadAsImage(subject)
            subject.sendMessage(img)
        } else if (msg.indexOf("是的") != -1) {
            val count = (1..10).random()
            val img = File(RainData.ShidePath).resolve("sd$count.gif").uploadAsImage(subject)
            subject.sendMessage(img)
        } else if (msg.indexOf("是呀") != -1) {
            val img = File(RainData.ShidePath).resolve("sd7.gif").uploadAsImage(subject)
            subject.sendMessage(img)
        } else if (msg == "新闻") {
            getImg(coroutineContext, event, URL("https://api.vvhan.com/api/60s"))
        } else if (msg == "菜单") {
            event.subject.sendMessage(
                "「食用指南」\n\n发送【来图】【白毛】【猫耳】【壁纸】获取随机美图\n" +
                        "发送【新闻】可以获取今天的60秒读懂世界\n发送【我的运势】可以了解今天的运势情况\n" +
                        "发送【禁言抽奖】可以获取随机时长禁言奖励\n\n-> By https://mili.cyou <-\nSee Also: https://github.com/milimoe"
            )
        }
        // 撤回
        if ((msg.indexOf("/撤回") != -1 || msg.indexOf("撤回；") != -1) && (senderID == RainData.Master || RainData.RecallAccessGroup.contains(senderID))) {
            val m = messageChain[QuoteReply]
            if (m != null) {
                m.recallSource()
                messageChain.recall()
            }
        }
        /**
         * OSM核心功能
         * 每日运势
         */
        if (msg.getLeftString(4) == "我的运势") {
            if (!dailys.containsKey(senderID)) {
                val random = (1..dailylist.count()).random()
                val daily = dailylist[random - 1]
                event.subject.sendMessage(sender.at() + " 你的今日运势是：\n$daily")
                dailys[senderID] = daily
                when (random) {
                    in 1..6 -> { // 大吉
                        val bz = (1..3).random()
                        val img = File(RainData.BaizhouPath).resolve("dj$bz.png").uploadAsImage(subject)
                        subject.sendMessage(img)
                    }
                    
                    in 7..11 -> { // 中吉
                        val bz = (1..2).random()
                        val img = File(RainData.BaizhouPath).resolve("zj$bz.png").uploadAsImage(subject)
                        subject.sendMessage(img)
                    }
                    
                    in 12..16 -> { // 吉
                        val bz = (1..4).random()
                        val img = File(RainData.BaizhouPath).resolve("j$bz.png").uploadAsImage(subject)
                        subject.sendMessage(img)
                    }
                    
                    in 17..23 -> { // 末吉
                        val bz = (1..2).random()
                        val img = File(RainData.BaizhouPath).resolve("mj$bz.png").uploadAsImage(subject)
                        subject.sendMessage(img)
                    }
                    
                    in 24..26 -> { // 大凶
                        val bz = (1..2).random()
                        val img = File(RainData.BaizhouPath).resolve("dx$bz.png").uploadAsImage(subject)
                        subject.sendMessage(img)
                    }
                    
                    in 27..30 -> { // 凶
                        val bz = (1..2).random()
                        val img = File(RainData.BaizhouPath).resolve("x$bz.png").uploadAsImage(subject)
                        subject.sendMessage(img)
                    }
                }
            } else {
                val daily = dailys[senderID]
                if (daily != null) {
                    event.subject.sendMessage(sender.at() + " 你的今日运势是：\n$daily")
                }
            }
        }
        if (msg.getLeftString(2) == "查看" && msg.getRightString(2) == "运势") {
            val mid = msg.getNumber()
            if (mid != "") {
                val m = event.group.getMember(mid.toLong())
                if (m != null) {
                    if (mid.toLong() == RainData.BOTQQ && !dailys.containsKey(RainData.BOTQQ))
                    {
                        dailys[RainData.BOTQQ] = dailylist[(1..dailylist.count()).random()]
                    }
                    if (dailys.containsKey(mid.toLong())) {
                        event.subject.sendMessage("${m.nick}（${m.id}）" + "的今日运势是：\n${dailys[m.id]}")
                    } else {
                        event.subject.sendMessage("TA今天还没有抽取运势哦，快去提醒TA！")
                    }
                } else {
                    event.subject.sendMessage("群内查无此人。")
                }
            }
        }
        if (msg == "重置运势" && senderID == RainData.Master) {
            org.milimoe.dailys.clear()
            RainBOT.logger.info { "每日运势已刷新" }
            event.subject.sendMessage("每日运势已刷新")
            org.milimoe.dailys[RainData.BOTQQ] = dailylist[(1..dailylist.count()).random()]
        }
        if (senderID == RainData.Master && msg.length != 4 && msg.getLeftString(2) == "重置" && msg.getRightString(2) == "运势") {
            var m = messageChain.serializeToMiraiCode()
            if (m.indexOf("[mirai:at:") == -1) {
                subject.sendMessage("命令格式不正确\n请遵守格式：重置@1@2..@N 运势")
                return
            } else {
                val o = "[mirai:at:"
                m = m.replace("重置", "").trim()
                m = m.replace("运势", "").trim()
                val count = m.countString(o)
                if (count == 0) return
                val list = ArrayList<Long>()
                for (i in 0..count) {
                    m = m.replaceFirst(o, "").trim()
                    val id = m.getAtQQNumber()
                    if (id != "") {
                        list.add(id.toLong())
                        m = m.replaceFirst(list.last().toString() + "]", "").trim()
                    }
                }
                if (list.isNotEmpty()) {
                    for (c in list) {
                        dailys.remove(c)
                        event.subject.sendMessage(subject.getMember(c)?.nick + "（${c}）的每日运势已刷新")
                    }
                } else {
                    subject.sendMessage("命令格式不正确\n请遵守格式：重置@1@2..@N 运势")
                }
            }
        }
        /**
         * OSM核心
         */
        if (msg == ".osm -info") {
            subject.sendMessage(
                "OSM插件运行状态：\n随机复读：" + RainData.IsRepeat.isOn() + "\n随机OSM：" + RainData.IsOSM.isOn() +
                        "\n随机反驳不：" + RainData.IsSayNo.isOn() + "\n禁言抽奖：" + RainData.IsMute.isOn() +
                        "\n随机复读概率：${RainData.PRepeat}%\n随机OSM概率：${RainData.POSM}%" +
                        "\n随机反驳不概率：${RainData.PSayNo}%\n禁言抽奖时长区间：${RainData.MuteTime[0]}至${RainData.MuteTime[1]}秒" +
                        "\n随机复读延迟区间：${RainData.RepeatDelay[0] / 1000}至${RainData.RepeatDelay[1] / 1000}秒"
            )
        } else if (msg == ".osm -muteaccess") {
            var m = "禁言权限组成员：\n"
            var count = 0
            for (i in RainData.MuteAccessGroup) {
                if (event.group.getMember(i) != null)
                {
                    m = m.plus("${i}\n")
                    count++
                }
            }
            m = if (count > 0) m.plus("本群共${count}名禁言权限组成员")
            else m.plus("本群无禁言权限组成员")
            subject.sendMessage(m)
        } else if (msg == ".osm -unmuteaccess") {
            var m = "解禁权限组成员：\n"
            var count = 0
            for (i in RainData.UnmuteAccessGroup) {
                if (event.group.getMember(i) != null)
                {
                    m = m.plus("${i}\n")
                    count++
                }
            }
            m = if (count > 0) m.plus("本群共${count}名解禁权限组成员")
            else m.plus("本群无解禁权限组成员")
            subject.sendMessage(m)
        } else if (msg == ".osm -recallaccess") {
            var m = "解禁权限组成员：\n"
            var count = 0
            for (i in RainData.RecallAccessGroup) {
                if (event.group.getMember(i) != null)
                {
                    m = m.plus("${i}\n")
                    count++
                }
            }
            m = if (count > 0) m.plus("本群共${count}名撤回权限组成员")
            else m.plus("本群无撤回权限组成员")
            subject.sendMessage(m)
        } else if (msg.getLeftString(4) == ".osm") {
            subject.sendMessage("OSM Core\nVersion: ${OSMCore.version}${OSMCore.version2}\nMaster: ${RainData.Master}\nAuthor: github.com/milimoe\nBuilt on ${OSMCore.time}")
        }
    }
}

fun Long.isOn(): String {
    return if (this == 1L) "开启"
    else "关闭"
}

fun String.getAtQQNumber(): String {
    val str = this.replace("[mirai:at:", "").trim()
    val sb = StringBuilder()
    for (c in str) {
        if (c in '0'..'9') {
            sb.append(c)
        }
        if (c == ']') break
    }
    return sb.toString()
}

fun String.countString(s: String): Int {
    val str = this.replace(s, "；")
    var count = 0
    for (c in str) {
        if (c == '；') count++
    }
    return count
}

fun String.getNumber(): String {
    val s = StringBuilder()
    for (c in this) {
        if (c in '0'..'9') {
            s.append(c)
        }
    }
    return s.toString()
}

fun String.getLeftString(len: Int): String {
    if (this.length >= len) {
        return this.substring(0, len)
    }
    return ""
}

fun String.getRightString(len: Int): String {
    if (this.length >= len) {
        return this.substring(length - len, length)
    }
    return ""
}

fun String.getSayNo(): String {
    val whereno = this.indexOf("不")
    val wheremei = this.indexOf("没")
    val whereshi = this.indexOf("是")
    val wherebie = this.indexOf("别")
    if (whereno >= 0 && whereno != this.length - 1) {
        var type: Int = 0
        var w = this[whereno + 1]
        logger.info { "触发了随机反驳不 -> $w" }
        if (whereno > 0) {
            val newmsg = this.substring(whereno, this.length)
            if ((this[whereno + 1] == '了' || this[whereno + 1] == '就' ||
                        this[whereno + 1] == '都' || this[whereno + 1] == '太' || this[whereno + 1] == '过') && this[whereno] != '这') {
                if (whereno + 2 < this.length) {
                    w = this[whereno + 2]
                    type = (0..2).random()
                    when (type) {
                        0 -> {
                            return "你${w}别人不一定${w}啊"
                        }
        
                        1 -> {
                            return "这都${w}？"
                        }
        
                        2 -> {
                            return "不懂就问，${w}了又能怎样呢？"
                        }
                    }
                } else {
                    w = this[whereno - 1]
                    type = (0..2).random()
                    when (type) {
                        0 -> {
                            return "你说不${w}就不${w}？"
                        }
        
                        1 -> {
                            return "不一定"
                        }
        
                        2 -> {
                            return "不想${w}可以不${w}"
                        }
                    }
                }
            }
            if (w == this[whereno - 1]) {
                type = (0..10).random()
                when (type) {
                    0 -> {
                        return "不${w}"
                    }
                    
                    1 -> {
                        return if (w == '是') "${w}的"
                        else "$w"
                    }
                    
                    2 -> {
                        return "我不好说"
                    }
                    
                    3 -> {
                        return "想${w}可以直接$w"
                    }
                    
                    4 -> {
                        return "我觉得是别$w"
                    }
                    
                    5 -> {
                        return "${w}不${w}不是你说了算的"
                    }
                    
                    6 -> {
                        return "是这样的"
                    }
    
                    7 -> {
                        return "${w}不${w}就别来问群友了吧，成年人了可以自己拿主意"
                    }
    
                    8 -> {
                        return "得了吧，没人在乎你${w}不${w}的"
                    }
    
                    9 -> {
                        return "你说的这个跟我没关系啊，别问我了"
                    }
    
                    10 -> {
                        return "从来不${w}"
                    }
                }
            } else if (newmsg.indexOf("吗") != -1 || newmsg.indexOf("呢") != -1 ||
                newmsg.indexOf("啊") != -1 || newmsg.indexOf("么") != -1 ||
                newmsg.indexOf("吧") != -1 || newmsg.indexOf("?") != -1 || newmsg.indexOf("？") != -1) {
                    type = (0..7).random()
                    when (type) {
                        0 -> {
                            return "不${w}"
                        }
                        
                        1 -> {
                            return "必不${w}"
                        }
                        
                        2 -> {
                            return "从来不${w}"
                        }
                        
                        3 -> {
                            return "不会有人不${w}吧？"
                        }
                        
                        4 -> {
                            return "看我心情"
                        }
                        
                        5 -> {
                            return "${w}又能怎样呢？"
                        }
                        
                        6 -> {
                            return "有一说一，确实"
                        }
                        
                        7 -> {
                            return "爱${w}不${w}"
                        }
                    }
                } else {
                    type = (0..30).random()
                    when (type) {
                        0 -> {
                            return "确实"
                        }
            
                        1 -> {
                            return "不是谁都${w}的"
                        }
            
                        2 -> {
                            return "就这"
                        }
                        
                        3 -> {
                        return "可是我${w}"
                        }
    
                        4 -> {
                            return "我也不${w}"
                        }
    
                        5 -> {
                            return "你不${w}不代表别人不${w}"
                        }
    
                        6 -> {
                            return "$w"
                        }
    
                        7 -> {
                            return "不想${w}可以不${w}"
                        }
    
                        8 -> {
                            return "想${w}可以直接${w}"
                        }
    
                        9 -> {
                            return "我觉得最好别${w}吧"
                        }
    
                        10 -> {
                            return "${w}不${w}不是你说了算的"
                        }
    
                        11 -> {
                            return "${w}不${w}就别来问群友了吧，成年人了可以自己拿主意"
                        }
    
                        12 -> {
                            return "不是，你觉得${w}不${w}对我有什么影响呢？"
                        }
    
                        13 -> {
                            return "不是很懂，你说下你为什么不想${w}吧"
                        }
    
                        14 -> {
                            return "太能${w}了"
                        }
    
                        15 -> {
                            return "太${w}了"
                        }
    
                        16 -> {
                            return "必${w}"
                        }
    
                        17 -> {
                            return "必不${w}"
                        }
    
                        18 -> {
                            return "从来不${w}"
                        }
    
                        19 -> {
                            return "${w}不${w}又能怎样？"
                        }
    
                        20 -> {
                            return "有一说一，确实"
                        }
    
                        21 -> {
                            return "爱${w}不${w}"
                        }
    
                        22 -> {
                            return "不${w}"
                        }
    
                        23 -> {
                            return "你为什么不想${w}？你才20岁啊"
                        }
    
                        24 -> {
                            return "笑了，真该${w}吧"
                        }
    
                        25 -> {
                            return "好${w}"
                        }
    
                        26 -> {
                            return "我就这么跟你说吧，骗哥们可以，别把你自己也骗到了。" +
                                    "哥们被你骗了无所谓的，打个哈哈就过了，但希望你打完这段话后擦一下眼角，别让眼泪掉到手机屏幕上了。"
                        }
    
                        27 -> {
                            return "只能说懂得都懂了兄弟们"
                        }
    
                        28 -> {
                            return "笑了，真没人${w}吧"
                        }
    
                        29 -> {
                            return "你今天不${w}我看不起你啊"
                        }
    
                        30 -> {
                            return "你凭啥不${w}啊？"
                        }
                    }
                }
        } else {
            if ((this[whereno + 1] == '了' || this[whereno + 1] == '就' ||
                    this[whereno + 1] == '都' || this[whereno + 1] == '太' || this[whereno + 1] == '过') && this[whereno] != '这') {
                if (whereno + 2 < this.length)
                {
                    w = this[whereno + 2]
                    type = (0..7).random()
                    when (type) {
                        0 -> {
                            return "你${w}别人不一定${w}啊"
                        }
        
                        1 -> {
                            return "这都${w}？"
                        }
        
                        2 -> {
                            return "不懂就问，${w}了又能怎样呢？"
                        }
        
                        3 -> {
                            return "想${w}可以直接${w}"
                        }
        
                        4 -> {
                            return "我觉得最好别${w}吧"
                        }
        
                        5 -> {
                            return "${w}不${w}不是你说了算的"
                        }
        
                        6 -> {
                            return "有一说一，是这样的"
                        }
        
                        7 -> {
                            return "确实"
                        }
                    }
                }
            } else {
                type = (0..25).random()
                when (type) {
                    0 -> {
                        return "可是我${w}"
                    }
        
                    1 -> {
                        return "我也不${w}"
                    }
        
                    2 -> {
                        return "你不${w}不代表别人不${w}"
                    }
        
                    3 -> {
                        return "$w"
                    }
        
                    4 -> {
                        return "不想${w}可以不${w}"
                    }
        
                    5 -> {
                        return "想${w}可以直接${w}"
                    }
                    
                    6 -> {
                        return "我觉得最好别${w}吧"
                    }
    
                    7 -> {
                        return "${w}不${w}不是你说了算的"
                    }
    
                    8 -> {
                        return "${w}不${w}就别来问群友了吧，成年人了可以自己拿主意"
                    }
    
                    9 -> {
                        return "不是，你觉得${w}不${w}对我有什么影响呢？"
                    }
    
                    10 -> {
                        return "不是很懂，你说下你为什么不想${w}吧"
                    }
    
                    11 -> {
                        return "太能${w}了"
                    }
    
                    12 -> {
                        return "太${w}了"
                    }
    
                    13 -> {
                        return "必${w}"
                    }
    
                    14 -> {
                        return "必不${w}"
                    }
    
                    15 -> {
                        return "从来不${w}"
                    }
    
                    16 -> {
                        return "${w}不${w}又能怎样？"
                    }
                    
                    17 -> {
                        return "有一说一，确实"
                    }
    
                    18 -> {
                        return "爱${w}不${w}"
                    }
    
                    19 -> {
                        return "你凭啥不${w}啊？"
                    }
    
                    20 -> {
                        return "笑了，真该${w}吧"
                    }
    
                    21 -> {
                        return "好${w}"
                    }
    
                    22 -> {
                        return "我就这么跟你说吧，骗哥们可以，别把你自己也骗到了。" +
                                "哥们被你骗了无所谓的，打个哈哈就过了，但希望你打完这段话后擦一下眼角，别让眼泪掉到手机屏幕上了。"
                    }
    
                    23 -> {
                        return "只能说懂得都懂了兄弟们"
                    }
    
                    24 -> {
                        return "笑了，真没人${w}吧"
                    }
    
                    25 -> {
                        return "你今天不${w}我看不起你啊"
                    }
                }
            }
        }
    } else if (wheremei >= 0 && wheremei != this.length - 1) {
        var type: Int = 0
        val w = this[wheremei + 1]
        if (w == '有') return "从来没有"
        logger.info { "触发了随机反驳没 -> $w" }
        type = (0..8).random()
        when (type) {
            0 -> {
                return "可是我有${w}"
            }
            
            1 -> {
                return "我也没${w}"
            }
            
            2 -> {
                return "你没${w}不代表别人没${w}"
            }
            
            3 -> {
                return "必没"
            }
            
            4 -> {
                return "我不好说"
            }
            
            5 -> {
                return "不会有人没${w}吧？"
            }
            
            6 -> {
                return "没${w}也就这样了"
            }
            
            7 -> {
                return "从来没有"
            }
            
            8 -> {
                return "这还没${w}，你想等到你80岁了才有${w}吗？"
            }
            
        }
    } else if (whereshi >= 0 && whereshi != this.length - 1) {
        val newmsg = this.substring(whereshi, this.length)
        if (newmsg.indexOf("吗") != -1 || newmsg.indexOf("呢") != -1 || newmsg.indexOf("呀") != -1 ||
            newmsg.indexOf("啊") != -1 || newmsg.indexOf("么") != -1 ||
            newmsg.indexOf("吧") != -1 || newmsg.indexOf("?") != -1 || newmsg.indexOf("？") != -1) {
            var type: Int = 0
            logger.info { "触发了随机反驳是" }
            type = (0..10).random()
            when (type) {
                0 -> {
                    return "是的"
                }
        
                1 -> {
                    return "不是"
                }
        
                2 -> {
                    return "是不是和我有关系吗？"
                }
        
                3 -> {
                    return "我觉得最好不是"
                }
        
                4 -> {
                    return "是不是又能怎样？"
                }
        
                5 -> {
                    return "osm"
                }
        
                6 -> {
                    return "应该是吧"
                }
        
                7 -> {
                    return "我觉得是"
                }
        
                8 -> {
                    return "你说是就是"
                }
        
                8 -> {
                    return "从来不是"
                }
        
                9 -> {
                    return "必不是"
                }
        
                10 -> {
                    return "真没人在乎是不是吧"
                }
        
            }
        }
    } else if (wherebie >= 0 && wherebie != this.length - 1) {
        var type: Int = 0
        val w = this[wherebie + 1]
        logger.info { "触发了随机反驳别 -> $w" }
        type = (0..6).random()
        when (type) {
            0 -> {
                return "算了吧，最好别${w}"
            }
        
            1 -> {
                return "从来不${w}"
            }
        
            2 -> {
                return "想${w}可以直接${w}"
            }
        
            3 -> {
                return "必不${w}"
            }
        
            4 -> {
                return "我不好说"
            }
        
            5 -> {
                return "不${w}明智之举"
            }
        
            6 -> {
                return "我觉得${w}不${w}都那样"
            }
        
        }
    }
    return ""
}

fun Char.isChinese(): Boolean {
    val ub: UnicodeBlock = UnicodeBlock.of(this)
    return ub === UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub === UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub === UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub === UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub === UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub === UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub === UnicodeBlock.GENERAL_PUNCTUATION
}

fun downloadImg(url: URL, imagePath: String, tryCount: Int): String {
    return if (tryCount > 0) try {
        val fileName = "${System.currentTimeMillis()}.jpg"
        // 基于NIO来下载网络上的图片
        FileChannel.open(
            Paths.get("$imagePath/$fileName"),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        ).use {
            it.transferFrom(Channels.newChannel(url.openStream()), 0, Long.MAX_VALUE)
        }
        fileName
    } catch (e: Exception) {
        logger.error("${e.javaClass.name} : $e.message")
        // 若发生网络异常，则进行有限次的重试
        downloadImg(url, imagePath, tryCount - 1)
    } else "err"
}

fun getImg(coroutineContext: CoroutineContext, event: GroupMessageEvent, url: URL) {
    var trycount = 3
    val scope = CoroutineScope(coroutineContext)
    // 启动一个下载图片，转发图片的协程
    scope.launch {
        var timeCost = System.currentTimeMillis()
        // 获取图片
        val fileName = downloadImg(
            url,
            RainData.ImgPath,
            3
        )
        timeCost = System.currentTimeMillis() - timeCost
        // 检查是否正确获取了图片，并发送对应的消息
        when (fileName) {
            "err" -> {
                logger.error("未能正确下载图片，尝试次数: $trycount，Time: $timeCost ms")
                event.subject.sendMessage("图片获取失败>_<")
            }
            
            else -> {
                logger.info("获取到图片 $fileName，Time: $timeCost ms")
                val img = File(RainData.ImgPath).resolve(fileName).uploadAsImage(event.group)
                event.subject.sendMessage(img)
            }
        }
    }
}
package org.milimoe.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.util.scopeWith
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.message.data.Image
import org.milimoe.RainBOT
import org.milimoe.data.RainData
import org.milimoe.data.RainSetting.IsOpenOSMGroup

// 简单指令
object RainSimpleCommand : SimpleCommand(
    RainBOT, "milimoe",
    description = "示例指令"
) {
    // 会自动创建一个 ID 为 "org.example.example-plugin:command.milimoe" 的权限.
    // 通过 /milimoe 调用, 参数自动解析
    @Handler
    suspend fun CommandSender.handle(option: String, param: String) { // 函数名随意, 但参数需要按顺序放置.
        if (this.hasPermission(RainBOT.PERMISSION_MILIMOE)) {
            var isSuccess = false
            when (option) {
                "master" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 123456..9999999999) {
                        isSuccess = true
                        RainData.Master = param.toLong()
                    }

                "bot" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 123456..9999999999) {
                        isSuccess = true
                        RainData.BOTQQ = param.toLong()
                    }

                "minmt" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 1..RainData.MuteTime[1]) {
                        isSuccess = true
                        RainData.MuteTime[0] = param.toLong()
                    }

                "maxmt" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in RainData.MuteTime[0]..2505600) {
                        isSuccess = true
                        RainData.MuteTime[1] = param.toLong()
                    }

                "mindt" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 1000..RainData.RepeatDelay[1]) {
                        isSuccess = true
                        RainData.RepeatDelay[0] = param.toLong()
                    }

                "maxdt" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in RainData.RepeatDelay[0]..600000) {
                        isSuccess = true
                        RainData.RepeatDelay[1] = param.toLong()
                    }

                "mute" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..1) {
                        isSuccess = true
                        RainData.IsMute = param.toLong()
                    }

                "sayno" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..1) {
                        isSuccess = true
                        RainData.IsSayNo = param.toLong()
                    }

                "repeat" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..1) {
                        isSuccess = true
                        RainData.IsRepeat = param.toLong()
                    }

                "osm" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..1) {
                        isSuccess = true
                        RainData.IsOSM = param.toLong()
                    }

                "psayno" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..100) {
                        isSuccess = true
                        RainData.PSayNo = param.toLong()
                    }

                "prepeat" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..100) {
                        isSuccess = true
                        RainData.PRepeat = param.toLong()
                    }

                "posm" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..100) {
                        isSuccess = true
                        RainData.POSM = param.toLong()
                    }

                "osmcore" ->
                    if (param.matches("-?\\d+(\\.\\d+)?".toRegex()) && param.toLong() in 0..100) {
                        val g = getGroupOrNull()
                        if (g != null) {
                            isSuccess = true
                            IsOpenOSMGroup.set.add(g.id)
                        }
                    }
            }
            if (!isSuccess) sendMessage("赋给${option}的参数无效：$param")
            else sendMessage("已设定${option}的值为：$param")
        }
    }

    @Handler
    suspend fun CommandSender.handle(option: String, id: Long, todo: String) {
        if (this.hasPermission(RainBOT.PERMISSION_MILIMOE)) {
            var isSuccess = false
            when (option) {
                "title" -> {
                    val bot = Bot.instances[0]
                    val g = getGroupOrNull()
                    if (g != null) {
                        if (g.getMember(bot.id) != null && g.getMember(bot.id)!!.isOwner()) {
                            val m = g.getMember(id)
                            if (m != null) {
                                if (todo.trim().length in 1..6) {
                                    m.specialTitle = todo
                                    isSuccess = true
                                }
                            }
                        } else {
                            sendMessage("设置头衔失败，BOT不是群主，请群主退位再试。")
                            isSuccess = true
                        }
                    }
                    if (!isSuccess) sendMessage("设置头衔失败，可能是群不存在或者是群查无此人。")
                }
                
                "send" -> {
                    val bot = Bot.instances[0]
                    val g = bot.getGroup(id)
                    if (g != null) {
                        if (g.getMember(bot.id) != null) {
                            if (todo != "" && todo.isNotEmpty()) {
                                g.sendMessage(todo)
                                isSuccess = true
                            }
                        }
                    }
                    if (!isSuccess) sendMessage("发送至群${id}失败。")
                }
                
                "osm" -> {
                    val bot = Bot.instances[0]
                    val g = bot.getGroup(id)
                    if (g != null) {
                        if (g.getMember(bot.id) != null) {
                            isSuccess = true
                            when (todo) {
                                "1" -> {
                                    IsOpenOSMGroup.set.add(id)
                                    sendMessage("群${id}开启了OSM核心。")
                                }
                                "0" -> {
                                    IsOpenOSMGroup.set.remove(id)
                                    sendMessage("群${id}关闭了OSM核心。")
                                }
                                else -> sendMessage("群${id}设置OSM核心状态失败，无效参数${todo}。")
                            }
                        }
                    }
                    if (!isSuccess) sendMessage("群${id}设置OSM核心状态失败。")
                }
                
                else -> sendMessage("找不到匹配的指令。")
            }
        }
    }
    
    // 复合指令
    object MiliCompositeCommand : CompositeCommand(
        RainBOT, "manage",
        description = "示例指令",
        // prefixOptional = true // 还有更多参数可填, 此处忽略
    ) {
        // 会自动创建一个 ID 为 "org.example.example-plugin:command.manage" 的权限.
        // [参数智能解析]
        //
        // 在控制台执行 "/manage <群号>.<群员> <持续时间>",
        // 或在聊天群内发送 "/manage <@一个群员> <持续时间>",
        // 或在聊天群内发送 "/manage <目标群员的群名> <持续时间>",
        // 或在聊天群内发送 "/manage <目标群员的账号> <持续时间>"
        // 时调用这个函数
        @SubCommand
        suspend fun CommandSender.mute(target: Member, duration: Int) { // 通过 /manage mute <target> <duration> 调用
            sendMessage("/manage mute 被调用了, 参数为: $target, $duration")

            val result = kotlin.runCatching {
                target.mute(duration).toString()
            }.getOrElse {
                it.stackTraceToString()
            } // 失败时返回堆栈信息

            // 表示对 this 和 ConsoleCommandSender 一起操作
            this.scopeWith(ConsoleCommandSender) {
                sendMessage("结果: $result") // 同时发送给 this@CommandSender 和 ConsoleCommandSender
            }
        }

        @SubCommand
        suspend fun CommandSender.list() { // 执行 "/manage list" 时调用这个函数
            sendMessage("/manage list 被调用了")
        }

        // 支持 Image 类型, 需在聊天中执行此指令.
        @SubCommand
        suspend fun CommandSender.test(image: Image) { // 执行 "/manage test <一张图片>" 时调用这个函数
            sendMessage("/manage image 被调用了, 图片是 ${image.imageId}")
        }
    }
}
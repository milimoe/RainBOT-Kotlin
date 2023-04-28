package org.milimoe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.milimoe.command.RainSimpleCommand
import org.milimoe.data.RainData
import org.milimoe.data.RainSetting
import org.milimoe.event.*
import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val dailys: HashMap<Long, String> = HashMap<Long, String>()
val repeats: HashMap<IntArray, MessageChain> = HashMap<IntArray, MessageChain>()
val whomute: HashMap<Long, Long> = HashMap<Long, Long>()

object RainBOT : KotlinPlugin(
    JvmPluginDescription(
        id = "org.milimoe.raincandy",
        name = "RainCandy",
        version = "1.2.1",
    ) {
        author("Milimoe")
    }
) {
    val PERMISSION_MILIMOE by lazy {
        PermissionService.INSTANCE.register(permissionId("Milimoe"), "Milimoe的权限")
    }

    override fun onEnable() {
        RainSetting.reload() // 从数据库自动读取配置实例
        RainData.reload()

        logger.info { "Hi: ${RainSetting.name}" } // 输出一条日志.
        //logger.verbose("Hi: ${MiliSetting.name}") // 多种日志级别可选

        RainSimpleCommand.register() // 注册指令
        setPath() // 设置各个文件的储存地址
        val dailylist:List<String> = RainSetting.daily.list // 运势内容列表

        Thread {
            var isrefresh = false
            var issaygoodmorning = false
            var issaygoodnight = false
            while (true) {
                Thread.sleep(1000)
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val formatted = current.format(formatter)
                if (formatted == "00:00") {
                    if (!isrefresh) {
                        isrefresh = true
                        dailys.clear()
                        logger.info { "每日运势已刷新" }
                        dailys[RainData.BOTQQ] = dailylist[(1..dailylist.count()).random()]
                    }
                } else {
                    if (isrefresh) isrefresh = false
                }
                if (formatted == "08:30")
                {
                    if (!issaygoodmorning)
                    {
                        issaygoodmorning = true
                        getNews()
                        logger.info { "一日之计在于晨" }
                    }
                } else {
                    if (issaygoodmorning) issaygoodmorning = false
                }
                if (formatted == "23:50")
                {
                    if (!issaygoodnight)
                    {
                        issaygoodnight = true
                        sayGoodNight()
                        logger.info { "早睡早起身体好" }
                    }
                } else {
                    if (issaygoodnight) issaygoodnight = false
                }
            }
        }.start()

        GlobalEventChannel.parentScope(this).subscribeAlways<BotOnlineEvent> {
            RainBOTBotOnline.load(this)
        }
        GlobalEventChannel.parentScope(this).subscribeAlways<MemberJoinRequestEvent> {
            RainBOTMemberJoinRequest.load(this)
        }
        GlobalEventChannel.parentScope(this).subscribeAlways<GroupMessageEvent> {
            MiraiBOTGroupMessage.load(RainBOT.coroutineContext, this, dailys, dailylist)
        }
        GlobalEventChannel.parentScope(this).subscribeAlways<FriendMessageEvent> {
            RainBOTFriendMessage.load(this)
        }
        GlobalEventChannel.parentScope(this).subscribeAlways<MemberMuteEvent> {
            RainBOTMemberMute.load(this)
        }
        GlobalEventChannel.parentScope(this).subscribeAlways<GroupTempMessageEvent> {
            RainBOTGroupTempMessage.load(this)
        }
        GlobalEventChannel.parentScope(this).subscribeAlways<MessageRecallEvent.GroupRecall> {
            RainBOTMemberRecall.load(this)
        }
    }

    override fun onDisable() {
        RainSimpleCommand.unregister() // 取消注册指令
    }

    fun getNews() {
        val bot = Bot.instances[0]
        if (bot.id != RainData.BOTQQ) return
        val groups: ContactList<Group> = bot.groups
        if (groups.isNotEmpty())
        {
            var trycount = 3
            val scope = CoroutineScope(coroutineContext)
            // 启动一个下载图片，转发图片的协程
            scope.launch {
                var timeCost = System.currentTimeMillis()
                // 获取图片
                val fileName = downloadImg(
                    URL("https://api.vvhan.com/api/60s"),
                    RainData.ImgPath,
                    3
                )
                timeCost = System.currentTimeMillis() - timeCost
                // 检查是否正确获取了图片，并发送对应的消息
                when (fileName) {
                    "err" -> {
                        logger.error("未能正确下载图片，尝试次数: $trycount，Time: $timeCost ms")
                        logger.info { "图片获取失败>_<" }
                    }

                    else -> {
                        logger.info("获取到图片 $fileName，Time: $timeCost ms")
                        for (g in groups) {
                            val img = File(RainData.ImgPath).resolve(fileName).uploadAsImage(g)
                            val chain = messageChainOf(PlainText("早上好早上好～").plus(img))
                            g.sendMessage(chain)
                        }
                    }
                }
            }
        }
    }

    private fun sayGoodNight() {
        val bot = Bot.instances[0]
        if (bot.id != RainData.BOTQQ) return
        CoroutineScope(coroutineContext).launch {
            val groups: ContactList<Group> = bot.groups
            val chain = messageChainOf(PlainText("Good Night～").plus("[mirai:face:75]".deserializeMiraiCode()).plus(PlainText("明天见\n现在睡觉可以保证到明天8点起床的时候有8小时的充足睡眠噢！")))
            for (g in groups) {
                g.sendMessage(chain)
            }
        }
    }

    private fun setPath() {
        // 创建图片存储区
        var name = "img"
        var folder = dataFolder.resolve(name)
        when {
            folder.exists() -> {
                logger.info("$name folder: ${folder.path}")
                RainData.ImgPath = folder.path
            }
            else -> {
                logger.info("Can't find $name folder")
                folder.mkdirs()
                logger.info("Create $name Folder: ${folder.path}")
                RainData.ImgPath = folder.path
            }
        }
        name = "longtu"
        folder = dataFolder.resolve(name)
        when {
            folder.exists() -> {
                logger.info("$name folder: ${folder.path}")
                RainData.LongtuPath = folder.path
            }
            else -> {
                logger.info("Can't find $name folder")
                folder.mkdirs()
                logger.info("Create $name Folder: ${folder.path}")
                RainData.LongtuPath = folder.path
            }
        }
        name = "dingzhen"
        folder = dataFolder.resolve(name)
        when {
            folder.exists() -> {
                logger.info("$name folder: ${folder.path}")
                RainData.DingzhenPath = folder.path
            }
            else -> {
                logger.info("Can't find $name folder")
                folder.mkdirs()
                logger.info("Create $name Folder: ${folder.path}")
                RainData.DingzhenPath = folder.path
            }
        }
        name = "baizhou"
        folder = dataFolder.resolve(name)
        when {
            folder.exists() -> {
                logger.info("$name folder: ${folder.path}")
                RainData.BaizhouPath = folder.path
            }
            else -> {
                logger.info("Can't find $name folder")
                folder.mkdirs()
                logger.info("Create $name Folder: ${folder.path}")
                RainData.BaizhouPath = folder.path
            }
        }
        name = "shide"
        folder = dataFolder.resolve(name)
        when {
            folder.exists() -> {
                logger.info("$name folder: ${folder.path}")
                RainData.ShidePath = folder.path
            }
            else -> {
                logger.info("Can't find $name folder")
                folder.mkdirs()
                logger.info("Create $name Folder: ${folder.path}")
                RainData.ShidePath = folder.path
            }
        }
        name = "yinpin"
        folder = dataFolder.resolve(name)
        when {
            folder.exists() -> {
                logger.info("$name folder: ${folder.path}")
                RainData.YinpinPath = folder.path
            }
            else -> {
                logger.info("Can't find $name folder")
                folder.mkdirs()
                logger.info("Create $name Folder: ${folder.path}")
                RainData.YinpinPath = folder.path
            }
        }
        logger.info("general folder: ${dataFolder.path}")
        RainData.GeneralPath = dataFolder.path
    }
}




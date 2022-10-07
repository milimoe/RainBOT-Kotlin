package org.milimoe.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.PluginDataExtensions.mapKeys
import net.mamoe.mirai.console.data.PluginDataExtensions.withEmptyDefault
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

// 定义插件数据
// 插件
object RainData : AutoSavePluginData("Milimoe") { // "name" 是保存的文件名 (不带后缀)
    var BOTQQ: Long by value(0L) // 机器人QQ
    var Master: Long by value(0L) // 主人QQ
    var Dingzhen: Long by value(0L) // 真图数量
    var Longtu: Long by value(0L) // 龙图数量
    var RepeatDelay: MutableList<Long> by value(mutableListOf(120000L, 300000L)) // 复读延迟
    var MuteTime: MutableList<Long> by value(mutableListOf(120L, 12600L)) // 禁言时长
    var IsRepeat: Long by value(1L) // 是否开启随机复读
    var IsOSM: Long by value(1L) // 是否开启随机OSM
    var IsSayNo: Long by value(1L) // 是否开启随机反驳不
    var IsMute: Long by value(1L) // 是否开启禁言抽奖
    var IsRun: Long by value(1L) // 是否运行
    var PRepeat: Long by value(7L) // 随机复读概率
    var POSM: Long by value(2L) // 随机OSM概率
    var PSayNo: Long by value(1L) // 随机反驳不概率
    var ImgPath: String by value("") // 临时图片储存地址（来图新闻之类的）
    var LongtuPath: String by value("") // 龙图储存地址
    var DingzhenPath: String by value("") // 丁真储存地址
    var BaizhouPath: String by value("") // 白洲储存地址
    var ShidePath: String by value("") // 是的储存地址
    var YinpinPath: String by value("") // 音频储存地址
    var GeneralPath: String by value("") // 其他文件储存地址
    
    val RepeatIgnore: HashSet<String>  = hashSetOf(
        "我的运势",
        "来图",
        "白毛",
        "猫耳",
        "壁纸",
        "新闻",
        "菜单",
        "白毛",
        "http:",
        "https:",
        ".com",
        ".cn",
        ".osm",
        "[at=all]",
        "[聊天记录]",
        "禁言抽奖",
        "撤回；",
        "/撤回",
        "白丝",
        "loli",
        "黑丝"
    )
    
    // 带默认值的非空 map.
    // notnullMap[1] 的返回值总是非 null 的 MutableMap<Int, String>
    var notnullMap
            by value<MutableMap<Int, MutableMap<Int, String>>>().withEmptyDefault()

    // 可将 MutableMap<Long, Long> 映射到 MutableMap<Bot, Long>.
    val botToLongMap: MutableMap<Bot, Long> by value<MutableMap<Long, Long>>().mapKeys(Bot::getInstance, Bot::id)
}

// 定义一个配置. 所有属性都会被追踪修改, 并自动保存.
// 配置是插件与用户交互的接口, 但不能用来保存插件的数据.
object RainSetting : ReadOnlyPluginConfig("Milimoe") { // "MySetting" 是保存的文件名 (不带后缀)
    val name by value("Mili")

    @ValueDescription("数量") // 注释, 将会保存在 MySetting.yml 文件中.
    val count by value(0)

    val daily by value<DailyData>() // 嵌套类型是支持的
}

object OSMCore {
    const val version = "v1.0"
    const val version2 = "patch4"
    const val time = "Oct. 8, 2022"
}

@Serializable
data class DailyData(
    val list: List<String> = listOf(
        "——大吉——\n会起风的日子，无论干什么都会很顺利的一天。\n周围的人心情也非常愉快，绝对不会发生冲突，\n还可以吃到一直想吃，但没机会吃的美味佳肴。\n无论是工作，还是旅行，都一定会十分顺利吧。\n那么，应当在这样的好时辰里，一鼓作气前进…\n\n今天的幸运物是：茁壮成长的「鸣草」。\n许多人或许不知道，鸣草是能预报雷暴的植物。\n向往着雷神大人的青睐，只在稻妻列岛上生长。\n摘下鸣草时酥酥麻麻的触感，据说和幸福的滋味很像。",
        "——大吉——\n宝剑出匣来，无往不利。出匣之光，亦能照亮他人。\n今日能一箭射中空中的猎物，能一击命中守卫要害。\n若没有目标，不妨四处转转，说不定会有意外之喜。\n同时，也不要忘记和倒霉的同伴分享一下好运气哦。\n\n今天的幸运物是：难得一见的「马尾」。\n马尾随大片荻草生长，但却更为挺拔。\n与傲然挺立于此世的你一定很是相配。",
        "——大吉——\n失而复得的一天。\n原本以为石沉大海的事情有了好的回应，\n原本分道扬镳的朋友或许可以再度和好，\n不经意间想起了原本已经忘记了的事情。\n世界上没有什么是永远无法挽回的，\n今天就是能够挽回失去事物的日子。\n\n今天的幸运物是：活蹦乱跳的「鬼兜虫」。\n鬼兜虫是爱好和平、不愿意争斗的小生物。\n这份追求平和的心一定能为你带来幸福吧。",
        "——大吉——\n浮云散尽月当空，逢此签者皆为上吉。\n明镜在心清如许，所求之事心想则成。\n合适顺心而为的一天，不管是想做的事情，\n还是想见的人，现在是行动起来的好时机。\n\n今天的幸运物是：不断发热的「烈焰花花蕊」。\n烈焰花的炙热来自于火辣辣的花心。\n万事顺利是因为心中自有一条明路。",
        "——大吉——\n今天是个上分的好日子啊好日子。\n顺手丢雷有可能炸死残血，写意混烟没准会豪取五杀。\n如果情况允许，不如试试盲狙，说不定有意外之喜。\n今天的你手感火热，s1mple来了也挡不住。\n不要忘记给失意的队友发枪，分享你的好运气哦~\n\n今天的幸运来自：带来好运的「高爆手雷」。\n总有人说，运气来了谁都挡不住。\n殊不知，运气也是实力的一部分。听说好运的人放出的烟火，会有不一样的色彩。\n不如……",
        "——大吉——\n晴朗无云的天气，心情也变得轻松起来。\n今天不会有很多工作，万事都顺顺利利。\n鼓起勇气，去做一直想做却没做的事情吧！\n向着目标不畏艰险而前进的人们，终将会拥有胜利的果实。\n\n今天的幸运来自：遇事不决「Rush B」。\n传统中不失革新，简洁里蕴含变化。\n富有形式美感的战术，最能激发人的潜力。",
        "——中吉——\n天上有云飘过的日子，天气令人十分舒畅。\n工作非常顺利，连午睡时也会想到好点子。\n突然发现，与老朋友还有其他的共同话题…\n——每一天，每一天都要积极开朗地度过——\n\n今天的幸运物是：色泽艳丽的「堇瓜」。\n人们常说表里如一是美德，\n但堇瓜明艳的外貌下隐藏着的是谦卑而甘甜的内在。",
        "——中吉——\n十年磨一剑，今朝示霜刃。\n恶运已销，身临否极泰来之时。\n苦练多年未能一显身手的才能，\n现今有了大展身手的极好机会。\n若是遇到阻碍之事，亦不必迷惘，\n大胆地拔剑，痛快地战斗一番吧。\n\n今天的幸运物是：生长多年的「海灵芝」。\n弱小的海灵芝虫经历多年的风风雨雨，才能结成海灵芝。\n为目标而努力前行的人们，最终也必将拥有胜利的果实。",
        "——中吉——\n今天会遇到比自己厉害的年轻人。\n请不要担心，他对你没有恶意。\n时间带走了你曾经的辉煌，反手把它们刻在了人生的计分板上。\n来不及感伤了，我们还要继续走下去，不是吗？\n\n今天的幸运来自：30岁的天才少年「f0rest」。\n瑞典CS的传奇人物，至今依旧在为自己而发光发热。\nOld soldiers never die, they just fade away.",
        "——中吉——\n平平淡淡的一天。\n生活最本真的味道就是无味。\n如果感到无聊的话，不如来两把csgo。\n体会下游戏中的杂陈。\n\n今天的幸运来自：一发致命的「AK47」。\n最平凡却最适用，相信会符合你的小心思。",
        "——中吉——\n如入林之深秋，忽见叶卷清空，徒留云影绰绰。\n今天也是如此，清淡静雅，却叫人欲罢不能。\n走出家门，漫无目的地沉醉吧。\n只带那颗赤子之心，足矣。\n\n今天的幸运来自：精准而优雅的「M4A1-S」。\n冷静高效的杀手，迷人却致命。\n最适合今天的你。",
        "——吉——\n明明没有什么特别的事情，却感到心情轻快的日子。\n在没注意过的角落可以找到本以为丢失已久的东西。\n食物比平时更加鲜美，路上的风景也令人眼前一亮。\n——这个世界上充满了新奇的美好事物——\n\n今天的幸运物是：散发暖意的「鸟蛋」。\n鸟蛋孕育着无限的可能性，是未来之种。\n反过来，这个世界对鸟蛋中的生命而言，\n也充满了令其兴奋的未知事物吧。\n要温柔对待鸟蛋喔。",
        "——吉——\n枯木逢春，正当万物复苏之时。\n陷入困境时，能得到解决办法。\n举棋不定时，会有贵人来相助。\n可以整顿一番心情，清理一番家装，\n说不定能发现意外之财。\n\n今天的幸运物是：节节高升的「竹笋」。\n竹笋拥有着无限的潜力，\n没有人知道一颗竹笋，到底能长成多高的竹子。\n看着竹笋，会让人不由自主期待起未来吧。",
        "——吉——\n一如既往的一天。身体和心灵都适应了的日常。\n出现了能替代弄丢的东西的物品，令人很舒心。\n和常常遇见的人关系会变好，可能会成为朋友。\n——无论是多寻常的日子，都能成为宝贵的回忆——\n\n今天的幸运物是：闪闪发亮的「晶核」。\n晶蝶是凝聚天地间的元素，而长成的细小生物。\n而元素是这个世界许以天地当中的人们的祝福。",
        "——吉——\n思维敏锐的一天，很适合学习。\n学点道具，学点思路，学点知识，学点能力。\n如果你正迷茫，抛开一切，去学习吧！\n\n今天的幸运来自：「开始努力的你」。\n千里之行，始于足下。\n只要开始，何时都不算晚。\n期待你成为那个能主宰自己一生的人。",
        "——吉——\n明明没有什么特别的事情，却依然会感到心情愉快。\n初次经历的事情也能做的十分优秀。\n游戏之前多开几张图吧，也许正是扩充图池的好机会。\n\n今天的幸运来自：「炼狱小镇的鸡」。\n你知道吗？近距离对准小鸡按「e」可以让小鸡跟着你走。\n细心又温柔的人运气不会差。\n对了，别忘了离那些拿着刀和手雷的队友远一点。",
        "——末吉——\n云遮月半边，雾起更迷离。\n抬头即是浮云遮月，低头则是浓雾漫漫。\n虽然一时前路迷惘，但也会有一切明了的时刻。\n现下不如趁此机会磨炼自我，等待拨云见皎月。\n\n今天的幸运物是：暗中发亮的「发光髓」。\n发光髓努力地发出微弱的光芒。\n虽然比不过其他光源，但看清前路也够用了。",
        "——末吉——\n空中的云层偏低，并且仍有堆积之势，\n不知何时雷雨会骤然从头顶倾盆而下。\n但是等雷雨过后，还会有彩虹在等着。\n宜循于旧，守于静，若妄为则难成之。\n\n今天的幸运物是：树上掉落的「松果」。\n并不是所有的松果都能长成高大的松树，\n成长需要适宜的环境，更需要一点运气。\n所以不用给自己过多压力，耐心等待彩虹吧。",
        "——末吉——\n平稳安详的一天。没有什么令人难过的事情会发生。\n适合和久未联系的朋友聊聊过去的事情，一同欢笑。\n吃东西的时候会尝到很久以前体验过的过去的味道。\n——要珍惜身边的人与事——\n\n今天的幸运物是：酥酥麻麻的「电气水晶」。\n电气水晶蕴含着无限的能量。\n如果能够好好导引这股能量，说不定就能成就什么事业。",
        "——末吉——\n气压稍微有点低，是会令人想到遥远的过去的日子。\n早已过往的年轻岁月，与再没联系过的故友的回忆，\n会让人感到一丝平淡的怀念，又稍微有一点点感伤。\n——偶尔怀念过去也很好。放松心情面对未来吧——\n\n今天的幸运物是：清新怡人的「薄荷」。\n只要有草木生长的空间，就一定有薄荷。\n这么看来，薄荷是世界上最强韧的生灵。\n据说连蒙德的雪山上也长着薄荷呢。",
        "——末吉——\n又是稀松平常的一天。\n萦绕在身旁的只有做不完的工作，\n和幽冷的烟火气。\n抬头看看远方，休息一下眼睛吧。\n\n今天的幸运来自：A小道的挚友「格洛克-18」。\n它很小，很不起眼。\n却是你重生时唯一的伙伴。\n格洛克很好，快说：谢谢格洛克。",
        "——末吉——\n容易急躁的一天，要稳住。\n高效率带来的可能会是漏洞，记得复查。\n今天可能会有大把空闲时间，注意合理安排。\n\n今天的幸运来自：高风险高回报的「AWP」。\n蛰伏，悄无声息，一击毙命。\n希望狙击带来的等待与沉稳能中和你今天的火气。",
        "——末吉——\n朝菌不知晦朔，蟪蛄不知春秋。\n认为自己全知全能者大有人在。\n退让乃大智，不要惩罚自己。\n\n今天的幸运来自：火力十足的「内格夫」。\n一时的输出不是一世的胜利。\n当他弹尽粮绝时，便是我反击之日。",
        "——大凶——\n内心空落落的一天。可能会陷入深深的无力感之中。\n很多事情都无法理清头绪，过于钻牛角尖则易生病。\n虽然一切皆陷于低潮谷底中，但也不必因此而气馁。\n若能撑过一时困境，他日必另有一番作为。\n\n今天的幸运物是：弯弯曲曲的「蜥蜴尾巴」\n蜥蜴遇到潜在的危险时，大多数会断尾求生。\n若是遇到无法整理的情绪，那么该断则断吧。",
        "——大凶——\n心情焦躁，仿佛被所有负面情绪包裹着。\n感觉天空灰蒙蒙的，也许很快就要下雨，别忘记带伞。当然，沙二除外。\n有些事情可能不会有结果，手握16000不发枪也是他的自由，要学会放手。\n身体是革命的本钱，一定保重，切记。\n\n今天的幸运来自：神出鬼没的「自由人」。\n好的自由人能帮助队伍走向胜利，别忘记检查对方出生点，没准会有宝藏在等着你。",
        "——大凶——\n感觉做什么都不顺利的一天。\n仿佛化身为Navi的电子哥本人，对手的道具就没歪过。\n\n今天的幸运来自：「生活」。\n即使是所谓的倒霉蛋，现实生活中也有着幸福美满的家庭。\n多陪陪家人，相信他们脸上的笑容会扫净一切阴霾。",
        "——凶——\n珍惜的东西可能会遗失，需要小心。\n如果身体有不适，一定要注意休息。\n在做出决定之前，一定要再三思考。\n\n今天的幸运物是：冰凉冰凉的「冰雾花」。\n冰雾花散发着「生人勿进」的寒气。\n但有时冰冷的气质，也能让人的心情与头脑冷静下来。\n据此采取正确的判断，明智地行动。",
        "——凶——\n隐约感觉会下雨的一天。可能会遇到不顺心的事情。\n应该的褒奖迟迟没有到来，服务生也可能会上错菜。\n明明没什么大不了的事，却总感觉有些心烦的日子。\n——难免有这样的日子——\n\n今天的幸运物是：随波摇曳的「海草」。\n海草是相当温柔而坚强的植物，\n即使在苦涩的海水中，也不愿改变自己。\n即使在逆境中，也不要放弃温柔的心灵。",
        "——凶——\n余姚冬瓜强提醒您：保护好您的显示器。\n人倒霉的时候，喝凉水都能塞牙。\n在做出重要的决策之前，请务必再三确认。\n\n今天的幸运来自：700元大狙「沙漠之鹰」。\ncsgo不能失去700大狙，就像西方不能失去耶路撒冷。\n在Nuke三楼使用沙鹰时请务必小心。",
        "——凶——\n马枪，有时在长时间游戏之后。\n手臂酸胀，手腕僵硬，好像肌肉被掏空。\n是不是太久没休息了？\n想把失去的枪法练回来？试试停稳再慢慢点射。\n你好，队友也好。\n\n今天的幸运来自：黑夜刺客「USP-S」。\nUSP之稳定在手枪中可以称得上无出其右。\n但是在古堡B包点却经常失灵，至今无法解释。")
)
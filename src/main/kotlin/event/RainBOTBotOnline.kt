package org.milimoe.event

import net.mamoe.mirai.event.events.BotOnlineEvent
import org.milimoe.data.RainData

object RainBOTBotOnline {
    suspend fun load(event: BotOnlineEvent) {
        if (event.bot.id != RainData.BOTQQ) return
    }
}
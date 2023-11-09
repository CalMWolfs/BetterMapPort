package com.calmwolfs.bettermap.data

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.events.ModChatEvent
import com.calmwolfs.bettermap.utils.StringUtils.removeResets
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ChatManager {
    private val config get() = BetterMapMod.feature.dev
    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        val original = event.message
        val message = original.formattedText.removeResets()

        if (message.startsWith("§f{\"server\":\"")) return
        if (config.locateSign && "Unable to locate sign at -?\\d+, -?\\d+, -?\\d+".toPattern().matcher(message).matches()) {
            return
        }

        val chatEvent = ModChatEvent(message, original)
        chatEvent.postAndCatch()
        val blockedReason =chatEvent.blockedReason

        if (blockedReason != "") {
            event.isCanceled = true
        }
    }
}
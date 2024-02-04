package com.calmwolfs.bettermap.data

import com.calmwolfs.bettermap.events.ActionBarUpdateEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import com.calmwolfs.bettermap.utils.StringUtils.removeResets
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ActionBarData {
    private var actionBar = ""

    fun getActionBar() = actionBar

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        actionBar = ""
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() != 2) return

        val original = event.message
        val message = original.formattedText.removeResets()
        if (message == actionBar) return
        actionBar = message
        ActionBarUpdateEvent(actionBar).postAndCatch()
    }
}
package com.calmwolfs.bettermap.data

import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.events.WorldChangeEvent
import net.minecraft.client.Minecraft
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class MinecraftData {
    private var tick = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        Minecraft.getMinecraft().thePlayer ?: return
        tick++
        ModTickEvent(tick).postAndCatch()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        WorldChangeEvent().postAndCatch()
    }
}
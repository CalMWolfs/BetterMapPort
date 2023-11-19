package com.calmwolfs.bettermap.utils

import com.calmwolfs.bettermap.events.EntityDeathEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EntityUtils {
    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        EntityDeathEvent(event.entity, event.entityLiving).postAndCatch()
    }
}
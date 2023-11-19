package com.calmwolfs.bettermap.events

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase

class EntityDeathEvent(val entity: Entity, val entityLiving: EntityLivingBase) : ModEvent()
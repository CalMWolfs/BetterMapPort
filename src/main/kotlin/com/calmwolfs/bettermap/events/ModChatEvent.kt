package com.calmwolfs.bettermap.events

import net.minecraft.util.IChatComponent

class ModChatEvent(var message: String, var component: IChatComponent, var blockedReason: String = "") : ModEvent()
package com.calmwolfs.bettermap.utils

import com.calmwolfs.BetterMap
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText

object ChatUtils {

    fun chat(message: String) {
        internalChat(message)
    }

    fun clickableChat(message: String, command: String) {
        val text = ChatComponentText(message)
        val fullCommand = "/" + command.removePrefix("/")
        text.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, fullCommand)
        text.chatStyle.chatHoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§eExecute $fullCommand"))
        messagePlayer(text)
    }

    fun internalChat(message: String): Boolean {
        val minecraft = Minecraft.getMinecraft()
        if (minecraft == null) {
            BetterMap.consoleLog(message.unformat())
            return false
        }

        val thePlayer = minecraft.thePlayer
        if (thePlayer == null) {
            BetterMap.consoleLog(message.unformat())
            return false
        }

        messagePlayer(ChatComponentText(message))
        return true
    }

    private fun messagePlayer(message: ChatComponentText) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(message)
    }
}
package com.calmwolfs.bettermap.commands

import com.calmwolfs.BetterMap
import com.calmwolfs.bettermap.utils.ChatUtils
import com.calmwolfs.bettermap.utils.ClipboardUtils
import com.calmwolfs.bettermap.utils.KeyboardUtils
import com.calmwolfs.bettermap.utils.StringUtils.unformat
import com.google.common.cache.CacheBuilder
import net.minecraft.client.Minecraft
import java.util.UUID
import java.util.concurrent.TimeUnit

object CopyErrorCommand {
    private val errorMessages = mutableMapOf<String, String>()
    private val fullErrorMessages = mutableMapOf<String, String>()
    private var cache =
        CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build<Pair<String, Int>, Unit>()

    fun command(array: Array<String>) {
        if (array.size != 1) {
            ChatUtils.chat("§cUse /bmcopyerror <error id> or just click on the error in chat!")
            return
        }

        val id = array[0]
        val fullError = KeyboardUtils.isControlKeyDown()
        val errorMessage = if (fullError) {
            fullErrorMessages[id]
        } else {
            errorMessages[id]
        }
        val name = if (fullError) "Full error" else "Error"
        ChatUtils.chat(errorMessage?.let {
            ClipboardUtils.copyToClipboard(it)
            "§e[BetterMap] $name copied into the clipboard, please report it on the Discord!"
        } ?: "§c[BetterMap] Error id not found!")
    }

    fun logError(throwable: Throwable, message: String) {
        val error = Error(message, throwable)
        error.printStackTrace()
        Minecraft.getMinecraft().thePlayer ?: return

        val pair = if (throwable.stackTrace.isNotEmpty()) {
            throwable.stackTrace[0].let { it.fileName to it.lineNumber }
        } else message to 0
        if (cache.getIfPresent(pair) != null) return
        cache.put(pair, Unit)

        val fullStackTrace = throwable.getExactStackTrace(true).joinToString("\n")
        val stackTrace = throwable.getExactStackTrace(false).joinToString("\n").removeSpam()
        val randomId = UUID.randomUUID().toString()

        val rawMessage = message.unformat()
        errorMessages[randomId] = "```\nBetterMap ${BetterMap.version}: $rawMessage\n \n$stackTrace\n```"
        fullErrorMessages[randomId] =
            "```\nBetterMap ${BetterMap.version}: $rawMessage\n(full stack trace)\n \n$fullStackTrace\n```"

        ChatUtils.clickableChat(
            "§c[BetterMap ${BetterMap.version}]: $message§c. Click here to copy the error into the clipboard.",
            "bmcopyerror $randomId"
        )
    }
}

private fun Throwable.getExactStackTrace(full: Boolean, parent: List<String> = emptyList()): List<String> = buildList {
    add("Caused by " + javaClass.name + ": $message")

    val breakAfter = listOf(
        "at net.minecraftforge.client.ClientCommandHandler.executeCommand(",
    )
    val replace = mapOf(
        "com.calmwolfs.bettermap" to "BM",
    )

    for (traceElement in stackTrace) {
        var text = "\tat $traceElement"
        if (!full) {
            if (text in parent) {
                println("broke at: $text")
                break
            }
        }
        if (!full) {
            for ((from, to) in replace) {
                text = text.replace(from, to)
            }
        }
        add(text)
        if (!full) {
            if (breakAfter.any { text.contains(it) }) {
                println("breakAfter: $text")
                break
            }
        }
    }

    cause?.let {
        addAll(it.getExactStackTrace(full, this))
    }
}

private fun String.removeSpam(): String {
    val ignored = listOf(
        "at io.netty.",
        "at net.minecraft.network.",
        "at net.minecraftforge.fml.common.network.handshake.",
        "at java.lang.Thread.run",
        "at com.google.gson.internal.",
        "at net.minecraftforge.fml.common.eventhandler.",
        "at java.util.concurrent.",
        "at sun.reflect.",
        "at net.minecraft.client.Minecraft.addScheduledTask(",
        "at java.lang.reflect.",
    )
    return split("\n").filter { line -> !ignored.any { line.contains(it) } }.joinToString("\n")
}
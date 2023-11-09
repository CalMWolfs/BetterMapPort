package com.calmwolfs.bettermap.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object StringUtils {
    private val whiteSpacePattern = "^\\s*|\\s*$".toPattern()
    private val resetPattern = "(?i)§R".toPattern()

    fun String.trimWhiteSpace(): String = whiteSpacePattern.matcher(this).replaceAll("")
    fun String.removeResets(): String = resetPattern.matcher(this).replaceAll("")

    fun String.unformat(): String {
        val builder = StringBuilder()

        var counter = 0
        while (counter < this.length) {
            if (this[counter] == '§') {
                counter += 2
            } else {
                builder.append(this[counter])
                counter++
            }
        }
        return builder.toString()
    }

    fun String.stripResets(): String {
        var message = this

        while (message.startsWith("§r")) {
            message = message.substring(2)
        }
        while (message.endsWith("§r")) {
            message = message.substring(0, message.length - 2)
        }
        return message
    }

    inline fun <T> Pattern.matchMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.matches()) consumer(it) else null }

    inline fun <T> Pattern.findMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.find()) consumer(it) else null }
}
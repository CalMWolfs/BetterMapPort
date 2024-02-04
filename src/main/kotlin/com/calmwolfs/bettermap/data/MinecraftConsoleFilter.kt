package com.calmwolfs.bettermap.data

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.utils.StringUtils.matchMatcher
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.message.Message

class MinecraftConsoleFilter : Filter {
    private val config get() = BetterMapMod.feature.dev

    private val biomeIdBoundsPattern = "Biome ID is out of bounds: (\\d+), defaulting to 0 \\(Ocean\\)".toPattern()
    private val removeTeamPattern = "net.minecraft.scoreboard.Scoreboard.removeTeam\\(Scoreboard.java:\\d+\\)".toPattern()
    private val createTeamPattern = "net.minecraft.scoreboard.Scoreboard.createTeam\\(Scoreboard.java:\\d+\\)".toPattern()
    private val removeObjectivePattern = "net.minecraft.scoreboard.Scoreboard.removeObjective\\(Scoreboard.java:\\d+\\)".toPattern()

    companion object {
        fun initLogging() {
            val ctx: LoggerContext = LogManager.getContext(false) as LoggerContext

            for (loggerConfig in ctx.configuration.loggers.values) {
                loggerConfig.addFilter(MinecraftConsoleFilter())
            }
        }
    }

    override fun filter(event: LogEvent?): Filter.Result {
        if (event == null) return Filter.Result.ACCEPT
        if (!config.filterLogs) return Filter.Result.ACCEPT

        val loggerName = event.loggerName
        if (loggerName == "SkyHanni") return Filter.Result.ACCEPT

        val message = event.message
        val formattedMessage = message.formattedMessage
        val thrown = event.thrown

        if (formattedMessage.startsWith("Needed to grow BufferBuilder buffer: Old size ")) {
            return Filter.Result.DENY
        }
        if (formattedMessage.startsWith("Unable to play unknown soundEvent: minecraft:")) {
            return Filter.Result.DENY
        }
        if (formattedMessage == "Could not spawn particle effect VILLAGER_HAPPY") {
            return Filter.Result.DENY
        }
        biomeIdBoundsPattern.matchMatcher(formattedMessage) {
            return Filter.Result.DENY
        }

        if (thrown != null) {
            val cause = thrown.cause
            if (cause != null && cause.stackTrace.isNotEmpty()) {
                val first = cause.stackTrace[0]
                val firstName = first.toString()
                removeTeamPattern.matchMatcher(firstName) { return Filter.Result.DENY }
                createTeamPattern.matchMatcher(firstName) { return Filter.Result.DENY }
                removeObjectivePattern.matchMatcher(firstName) { return Filter.Result.DENY }
            }
            if (thrown.toString().contains(" java.lang.IllegalArgumentException: A team with the name '")) {
                return Filter.Result.DENY
            }
        }

        return Filter.Result.ACCEPT
    }

    override fun getOnMismatch(): Filter.Result {
        return Filter.Result.DENY
    }

    override fun getOnMatch(): Filter.Result {
        return Filter.Result.ACCEPT
    }

    override fun filter(
        logger: Logger?,
        level: Level?,
        marker: Marker?,
        msg: String?,
        vararg params: Any?,
    ): Filter.Result {
        return Filter.Result.ACCEPT
    }

    override fun filter(
        logger: Logger?,
        level: Level?,
        marker: Marker?,
        msg: Any?,
        t: Throwable?,
    ): Filter.Result {
        return Filter.Result.ACCEPT
    }

    override fun filter(
        logger: Logger?,
        level: Level?,
        marker: Marker?,
        msg: Message?,
        t: Throwable?,
    ): Filter.Result {
        return Filter.Result.ACCEPT
    }
}
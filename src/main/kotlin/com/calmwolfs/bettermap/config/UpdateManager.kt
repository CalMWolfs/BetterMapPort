package com.calmwolfs.bettermap.config

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.config.update.ConfigVersionDisplay
import com.calmwolfs.bettermap.config.update.GuiOptionEditorUpdateCheck
import com.calmwolfs.bettermap.events.ConfigLoadEvent
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.utils.MinecraftExecutor
import com.calmwolfs.bettermap.utils.ModUtils.onToggle
import io.github.moulberry.moulconfig.processor.MoulConfigProcessor
import moe.nea.libautoupdate.CurrentVersion
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateSource
import moe.nea.libautoupdate.UpdateTarget
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CompletableFuture

object UpdateManager {
    private var _activePromise: CompletableFuture<*>? = null
    private var activePromise: CompletableFuture<*>?
        get() = _activePromise
        set(value) {
            _activePromise?.cancel(true)
            _activePromise = value
        }

    var updateState: UpdateState = UpdateState.NONE
        private set

    fun getNextVersion(): String? {
        return potentialUpdate?.update?.versionNumber?.asString
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        BetterMapMod.feature.about.updateStream.onToggle {
            reset()
        }
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        Minecraft.getMinecraft().thePlayer ?: return
        MinecraftForge.EVENT_BUS.unregister(this)
        if (config.autoUpdates)
            checkUpdate()
    }

    fun getCurrentVersion(): String {
        return BetterMapMod.version
    }

    fun injectConfigProcessor(processor: MoulConfigProcessor<*>) {
        processor.registerConfigEditor(ConfigVersionDisplay::class.java) { option, _ ->
            GuiOptionEditorUpdateCheck(option)
        }
    }

    private fun isPreRelease(): Boolean {
        return getCurrentVersion().contains("pre", ignoreCase = true)
    }

    private val config get() = BetterMapMod.feature.about

    private fun reset() {
        updateState = UpdateState.NONE
        _activePromise = null
        potentialUpdate = null
        println("Reset update state")
    }

    //todo reenable when needed, commenting to prevent a rare crash on launch
    fun checkUpdate() {
//        if (updateState != UpdateState.NONE) {
//            println("Trying to perform update check while another update is already in progress")
//            return
//        }
//        println("Starting update check")
//        var updateStream = config.updateStream.get()
//        if (updateStream == AboutConfig.UpdateStream.RELEASES && isPreRelease()) {
//            updateStream = AboutConfig.UpdateStream.PRE
//        }
//        activePromise = context.checkUpdate(updateStream.stream)
//            .thenAcceptAsync({
//                println("Update check completed")
//                if (updateState != UpdateState.NONE) {
//                    println("This appears to be the second update check. Ignoring this one")
//                    return@thenAcceptAsync
//                }
//                potentialUpdate = it
//                if (it.isUpdateAvailable) {
//                    updateState = UpdateState.AVAILABLE
//                    ChatUtils.clickableChat(
//                        "§e[BetterMap] §afound a new update: ${getNextVersion()} " +
//                                "§aGo check §b/bm download §afor more info.",
//                        "bm download"
//                    )
//                }
//            }, MinecraftExecutor.OnThread)
    }

    fun queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            println("Trying to enqueue an update while another one is already downloaded or none is present")
        }
        updateState = UpdateState.QUEUED
        activePromise = CompletableFuture.supplyAsync {
            println("Update download started")
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync({
            println("Update download completed, setting exit hook")
            updateState = UpdateState.DOWNLOADED
            potentialUpdate!!.executeUpdate()
        }, MinecraftExecutor.OnThread)
    }

    private val context = UpdateContext(
        UpdateSource.githubUpdateSource("CalMWolfs", "BetterMapPort"),
        UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager::class.java),
        CurrentVersion.ofTag(BetterMapMod.version),
        BetterMapMod.MODID,
    )

    init {
        context.cleanup()
    }

    enum class UpdateState {
        AVAILABLE,
        QUEUED,
        DOWNLOADED,
        NONE
    }

    private var potentialUpdate: PotentialUpdate? = null
}
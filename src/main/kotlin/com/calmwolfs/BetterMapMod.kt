package com.calmwolfs

import com.calmwolfs.bettermap.commands.Commands
import com.calmwolfs.bettermap.config.ConfigFileType
import com.calmwolfs.bettermap.config.ConfigManager
import com.calmwolfs.bettermap.config.Features
import com.calmwolfs.bettermap.config.PlayerData
import com.calmwolfs.bettermap.config.RepoManager
import com.calmwolfs.bettermap.config.UpdateManager
import com.calmwolfs.bettermap.data.ActionBarData
import com.calmwolfs.bettermap.data.ChatManager
import com.calmwolfs.bettermap.data.MinecraftConsoleFilter
import com.calmwolfs.bettermap.data.MinecraftData
import com.calmwolfs.bettermap.data.ScoreboardData
import com.calmwolfs.bettermap.data.TablistData
import com.calmwolfs.bettermap.data.WorldData
import com.calmwolfs.bettermap.data.connection.BetterMapServer
import com.calmwolfs.bettermap.data.mapdata.MapTeam
import com.calmwolfs.bettermap.data.roomdata.RoomDataFile
import com.calmwolfs.bettermap.data.roomdata.RoomDataManager
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.utils.DungeonUtils
import com.calmwolfs.bettermap.utils.EntityUtils
import com.calmwolfs.bettermap.utils.HypixelUtils
import com.calmwolfs.bettermap.utils.InventoryUtils
import com.calmwolfs.bettermap.utils.MapUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(
    modid = BetterMapMod.MODID,
    clientSideOnly = true,
    useMetadata = true,
    guiFactory = "com.calmwolfs.bettermap.config.gui.ConfigGuiForgeInterop",
    version = "1.0",
)
class BetterMapMod {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        loadModule(this)

        //other
        loadModule(ChatManager)
        loadModule(UpdateManager)

        //utils
        loadModule(DungeonUtils)
        loadModule(EntityUtils)
        loadModule(HypixelUtils)
        loadModule(InventoryUtils)
        loadModule(MapUtils)

        //data
        loadModule(ActionBarData)
        loadModule(BetterMapServer)
        loadModule(MapTeam)
        loadModule(MinecraftData())
        loadModule(PlayerData)
        loadModule(RoomDataManager)
        loadModule(ScoreboardData)
        loadModule(TablistData)
        loadModule(WorldData)

        //features

        //commands
        Commands.init()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        configManager = ConfigManager
        configManager.firstLoad()
        MinecraftConsoleFilter.initLogging()
        Runtime.getRuntime().addShutdownHook(Thread {
            configManager.saveConfig(ConfigFileType.FEATURES, "shutdown-hook")
        })
        repo = RepoManager(configManager.configDirectory)
        try {
            repo.loadRepoInformation()
        } catch (e: Exception) {
            Exception("Error reading repo data", e).printStackTrace()
        }
    }

    private fun loadModule(obj: Any) {
        modules.add(obj)
        MinecraftForge.EVENT_BUS.register(obj)
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (screenToOpen != null) {
            Minecraft.getMinecraft().displayGuiScreen(screenToOpen)
            screenToOpen = null
        }
    }

    companion object {
        const val MODID = "bettermap"

        @JvmStatic
        val version: String get() = Loader.instance().indexedModList[MODID]!!.version

        @JvmStatic
        val feature: Features get() = configManager.features
        val roomData: RoomDataFile get() = configManager.roomData

        lateinit var repo: RepoManager
        lateinit var configManager: ConfigManager

        private val logger: Logger = LogManager.getLogger("BetterMap")
        fun consoleLog(message: String) {
            logger.log(Level.INFO, message)
        }

        private val modules: MutableList<Any> = ArrayList()
        private val globalJob: Job = Job(null)

        val coroutineScope = CoroutineScope(
            CoroutineName("BetterMap") + SupervisorJob(globalJob)
        )

        var screenToOpen: GuiScreen? = null
    }
}

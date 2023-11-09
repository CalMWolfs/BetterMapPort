package com.calmwolfs.bettermap.config

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.data.ModVector
import com.calmwolfs.bettermap.data.roomdata.RoomDataFile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.moulberry.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.moulberry.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.moulberry.moulconfig.processor.ConfigProcessorDriver
import io.github.moulberry.moulconfig.processor.MoulConfigProcessor
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.concurrent.fixedRateTimer

object ConfigManager {
    val gson: Gson = GsonBuilder().setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
        .registerTypeAdapter(ModVector::class.java, object : TypeAdapter<ModVector>() {
            override fun write(out: JsonWriter, value: ModVector) {
                value.run { out.value("$x:$y:$z") }
            }

            override fun read(reader: JsonReader): ModVector {
                val (x, y, z) = reader.nextString().split(":").map { it.toDouble() }
                return ModVector(x, y, z)
            }
        }.nullSafe())
        .enableComplexMapKeySerialization()
        .create()

    lateinit var features: Features
        private set
    lateinit var roomData: RoomDataFile
        private set

    var configDirectory = File("config/bettermap")
    lateinit var processor: MoulConfigProcessor<Features>

    private var configFile: File? = null
    private var roomDataFile: File? = null

    fun firstLoad() {
        if (::features.isInitialized) {
            println("Loading config despite config being already loaded?")
        }
        configDirectory.mkdir()

        configFile = File(configDirectory, "config.json")
        roomDataFile = File(configDirectory, "room_data.json")

        features = firstLoadFile(configFile, ConfigFileType.FEATURES, Features())
        roomData = firstLoadFile(roomDataFile, ConfigFileType.ROOM_DATA, RoomDataFile())

        fixedRateTimer(name = "bettermap-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            saveConfig(ConfigFileType.FEATURES, "auto-save-60s")
        }

        val features = BetterMapMod.feature
        processor = MoulConfigProcessor(BetterMapMod.feature)
        BuiltinMoulConfigGuis.addProcessors(processor)
        UpdateManager.injectConfigProcessor(processor)
        ConfigProcessorDriver.processConfig(
            features.javaClass,
            features,
            processor
        )
    }

    private inline fun <reified T> firstLoadFile(file: File?, fileType: ConfigFileType, defaultValue: T): T {
        val fileName = fileType.fileName
        println("Trying to load $fileName from $file")
        var output: T = defaultValue

        if (file!!.exists()) {
            try {
                val inputStreamReader = InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)
                val bufferedReader = BufferedReader(inputStreamReader)

                println("load-$fileName-now")
                output = gson.fromJson(bufferedReader.readText(), T::class.java)

                println("Loaded $fileName from file")
            } catch (error: Exception) {
                error.printStackTrace()
                val backupFile = file.resolveSibling("$fileName-${System.currentTimeMillis()}-backup.json")
                println("Exception while reading $file. Will load blank $fileName and save backup to $backupFile")
                println("Exception was $error")
                try {
                    file.copyTo(backupFile)
                } catch (e: Exception) {
                    println("Could not create backup for $fileName file")
                    e.printStackTrace()
                }
            }
        }

        if (output == defaultValue) {
            println("Setting $fileName to be blank as it did not exist. It will be saved once something is written to it")
        }

        return output
    }

    fun saveConfig(fileType: ConfigFileType, reason: String) {
        when (fileType) {
            ConfigFileType.FEATURES -> saveFile(configFile, fileType.fileName, BetterMapMod.feature, reason)
            ConfigFileType.ROOM_DATA -> saveFile(roomDataFile, fileType.fileName, BetterMapMod.roomData, reason)
        }
    }

    private fun saveFile(file: File?, fileName: String, data: Any, reason: String) {
        println("saveConfig: $reason")
        if (file == null) throw Error("Can not save $fileName, ${fileName}File is null!")
        try {
            println("Saving $fileName file")
            file.parentFile.mkdirs()
            val unit = file.parentFile.resolve("$fileName.json.write")
            unit.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8)).use { writer ->
                writer.write(gson.toJson(data))
            }
            Files.move(
                unit.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (e: IOException) {
            println("Could not save $fileName file to $file")
            e.printStackTrace()
        }
    }
}

enum class ConfigFileType(val fileName: String) {
    FEATURES("config"),
    ROOM_DATA("room_data");
}

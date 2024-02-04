package com.calmwolfs.bettermap.data.connection

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.data.mapdata.MapTeam
import com.calmwolfs.bettermap.events.ModTickEvent
import com.calmwolfs.bettermap.utils.JsonUtils.asBooleanOrFalse
import com.calmwolfs.bettermap.utils.JsonUtils.getIntOrValue
import com.calmwolfs.bettermap.utils.JsonUtils.getStringOrValue
import com.calmwolfs.bettermap.utils.MapUtils
import com.calmwolfs.bettermap.utils.SimpleTimeMark
import com.google.gson.JsonObject
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

object BetterMapServer : SoopyCommunicator(SoopyPacketServer.BETTERMAP) {
    private var lastDataSend = SimpleTimeMark.farPast()

    private val gson get() = BetterMapMod.configManager.gson

    private val peopleUsingBMapCallback = ConcurrentHashMap<Int, (List<Boolean>) -> Unit>()

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!event.repeatSeconds(5)) return
        if (lastDataSend.passedSince() > 5.seconds) {
            peopleUsingBMapCallback.clear()
        }
    }

    fun start() {
        if (!this.isConnected()) {
            initialise(this)
        } else {
            println("bm server connection already established")
        }
    }

    override fun receiveData(data: JsonObject) {
        val type = data.getStringOrValue("type", "unknown")
        when (type) {
            "queryUsingBMap" -> {
                val id = data.getIntOrValue("id")

                val booleanArray = data.getAsJsonArray("data") ?: run {
                    peopleUsingBMapCallback.remove(id)
                    return
                }

                peopleUsingBMapCallback[id]?.let { callback ->
                    callback(booleanArray.map { it.asBooleanOrFalse() })
                    peopleUsingBMapCallback.remove(id)
                }
                return
            }
            "roomSecrets" -> {
                MapUtils.updateSecrets(data)
                return
            }
            "roomId" -> {
                MapUtils.updateRoomId(data)
                return
            }
        }
    }

    fun isUsingBMap(players: List<String>, callback: (List<Boolean>) -> Unit) {
        val uniqueId = (1..999999).random()

        val data = JsonObject()
        val array = gson.toJsonTree(players).asJsonArray
        data.add("queryUsingBMap", array)
        data.addProperty("id", uniqueId)

        lastDataSend = SimpleTimeMark.now()
        sendData(data)

        peopleUsingBMapCallback[uniqueId] = callback
    }

    fun sendDungeonData(type: String, vararg dataValues: Pair<String, Any>){
        val data = JsonObject()
        data.addProperty("type", type)
        for ((key, value) in dataValues) {
            when (value) {
                is Int -> data.addProperty(key, value)
                is String -> data.addProperty(key, value)
                is List<*> -> {
                    val array = gson.toJsonTree(value).asJsonArray
                    data.add(key, array)
                }
            }
        }

        val output = JsonObject()


        val players = MapTeam.getMapPlayers().map { it.value.username }
        val array = gson.toJsonTree(players).asJsonArray

        output.add("data", data)
        output.add("players", array)

        sendData(output)
    }
}
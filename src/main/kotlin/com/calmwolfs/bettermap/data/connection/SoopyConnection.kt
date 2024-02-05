package com.calmwolfs.bettermap.data.connection

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.utils.ChatUtils
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

object SoopyConnection {
    private val gson get() = BetterMapMod.configManager.gson
    private var socket: Socket? = null
    private var connected = false
    private var reconDelay = 1000.0

    private var output: OutputStream? = null
    private var writer: PrintWriter? = null

    private val dataToSend = mutableListOf<String>()

    private val handlers = mutableMapOf<SoopyPacketServer, SoopyCommunicator>()

    private var gameRunning = true
    private var connectedFull = false

    init {
        BetterMapMod.coroutineScope.launch {
            connect()

            while (gameRunning) {
                if (connected && socket != null) {
                    if (dataToSend.isNotEmpty()) {
                        for (line in dataToSend) {
                            writer?.println(line)
//                            ChatUtils.chat("sent (mod): $line")
                        }
                        dataToSend.clear()
                    } else {
                        delay(100)
                    }
                } else {
                    delay(1000)
                }
            }
        }
    }

    private suspend fun connect() {
        if (!gameRunning) return
        if (connected) return

        connectedFull = false
        println("connecting to soopy socket")

        try {
            socket = Socket("soopy.dev", 9898)
        } catch (e: Exception) {
            e.printStackTrace()
            println("socket error: ${e.message}")
            println("reconnecting in $reconDelay ms")
            delay(reconDelay.milliseconds)
            reconDelay *= 1.5
            connect()
            return
        }
        println("successfully connected")
        connected = true
        reconDelay = 1000.0

        output = socket?.getOutputStream()
        writer = output?.let { PrintWriter(it, true) }

        BetterMapMod.coroutineScope.launch {
            val input = socket?.getInputStream() ?: return@launch
            val reader = BufferedReader(InputStreamReader(input))
            var shouldContinue = true

            while (connected && socket != null && shouldContinue) {
                try {
                    val data = reader.readLine()
                    if (data != null) {
                        try {
                            val asJson = gson.fromJson(data, JsonObject::class.java)
                            val packet = asJson.toSoopyPacket()
                            receiveData(packet)
//                            ChatUtils.chat("received (mod): $data")
                        } catch (e: Exception) {
                            println("json error with: $data")
                        }
                    }
                } catch (e: Exception) {
                    println("SOCKET ERROR")
                    println(e.toString())
                    disconnect()
                    delay(5000)
                    println("Attempting to reconnect to the server")
                    shouldContinue = false
                    connect()
                }
            }

            if (connected && shouldContinue) {
                delay(1000)
                println("Attempting to reconnect to the server")
                connect()
            }
        }
    }

    private suspend fun receiveData(packet: SoopyPacket) {
        when (packet.type) {
            SoopyPacketType.SUCCESS -> {

                //authentication through microsoft like existing mods do (neu, st, soopy, bettermap, etc.)
                val serverId = UUID.randomUUID().toString().replace("-", "")
                val session = Minecraft.getMinecraft().session
                Minecraft.getMinecraft().sessionService.joinServer(session.profile, session.token, serverId)

                val data = JsonObject()
                data.addProperty("username", Minecraft.getMinecraft().thePlayer.name)
                data.addProperty("uuid", Minecraft.getMinecraft().thePlayer.uniqueID.toString())
                data.addProperty("serverId", serverId)

                sendData(SoopyPacket(SoopyPacketType.SUCCESS, SoopyPacketServer.API, data))

                for (handler in handlers.values) {
                    if (handler.isConnected()) continue
                    handler.openConnection()
                }
                connectedFull = true
            }

            SoopyPacketType.DATA -> {
                if (handlers.containsKey(packet.server)) {
                    handlers[packet.server]?.transmitData(packet.data)
                }
            }

            SoopyPacketType.SERVER_REBOOT -> {
                disconnect()
                delay(5000)
                connect()
            }

            SoopyPacketType.PING -> {
                sendData(SoopyPacket(SoopyPacketType.PING, SoopyPacketServer.API, JsonObject()))
            }

            else -> {}
        }
    }

    fun sendData(packet: SoopyPacket) {
        if (!connected || socket == null) return
        val data = packet.toJsonObject()
        dataToSend.add(data.toString().replace("\n", ""))
    }

    private fun disconnect() {
        socket?.close()
        socket = null
        connected = false
        println("disconnected with socket")
    }

    fun addHandler(handler: SoopyCommunicator) {
        handlers[handler.serverType] = handler

        if (connectedFull) {
            handler.openConnection()
        }
    }

    fun createDataPacket(server: SoopyPacketServer, data: JsonObject): SoopyPacket {
        return SoopyPacket(SoopyPacketType.DATA, server, data)
    }
}

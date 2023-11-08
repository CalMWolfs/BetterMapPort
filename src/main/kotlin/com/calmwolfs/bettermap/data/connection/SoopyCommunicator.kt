package com.calmwolfs.bettermap.data.connection

import com.calmwolfs.BetterMap
import com.google.gson.JsonObject

open class SoopyCommunicator(val serverType: SoopyPacketServer) {
    private var connected = false

    open fun initialise(communication: SoopyCommunicator) {
        SoopyConnection.addHandler(communication)
        connected = false
    }

    fun openConnection() {
        val modVersion = "Forge-${BetterMap.version}"
        val jsonObject = JsonObject()
        jsonObject.addProperty("version", modVersion)

        val packet = SoopyPacket(SoopyPacketType.JOIN_SERVER, this.serverType, jsonObject)

        SoopyConnection.sendData(packet)

        try {
            onConnect()
            connected = true
        } catch (e: Exception) {
            println("error while connecting for type: $serverType")
        }
    }

    fun transmitData(data: JsonObject) {
        try {
            receiveData(data)
        } catch (e: Exception) {
            println("error while passing data for type: $serverType")
            println("data: $data")
        }
    }

    fun sendData(data: JsonObject) {
        val packet = SoopyConnection.createDataPacket(this.serverType, data)
        SoopyConnection.sendData(packet)
    }

    fun isConnected() = connected

    open fun onConnect() {}
    open fun receiveData(data: JsonObject) {}
}

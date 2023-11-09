package com.calmwolfs.bettermap.utils

import com.calmwolfs.BetterMapMod
import com.calmwolfs.bettermap.commands.CopyErrorCommand
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils

object ApiUtils {
    private val parser = JsonParser()

    private val builder: HttpClientBuilder =
        HttpClients.custom().setUserAgent("BetterMap/Forge-${BetterMapMod.version}")
            .setDefaultHeaders(
                mutableListOf(
                    BasicHeader("Pragma", "no-cache"),
                    BasicHeader("Cache-Control", "no-cache")
                )
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .build()
            )
            .useSystemProperties()

    // is json array unlike a lot of api responses. If other apis used make sure to adjust
    private fun getJSONResponse(urlString: String): JsonArray {
        val client = builder.build()

        try {
            val response = client.execute(HttpGet(urlString))
            val entity = response.entity
            val retSrc = EntityUtils.toString(entity)

            return try {
                parser.parse(retSrc) as JsonArray
            } catch (e: JsonSyntaxException) {
                CopyErrorCommand.logError(e, "Api JSON syntax error")
                JsonArray()
            }
        } catch (e: Exception) {
            CopyErrorCommand.logError(e, "Api error")
            JsonObject()
        } finally {
            client.close()
        }
        return JsonArray()
    }

    suspend fun loadFromAPI(): JsonArray {
        val url = "https://soopy.dev/api/bettermap/roomdata"
        return withContext(Dispatchers.IO) { getJSONResponse(url) }.asJsonArray ?: JsonArray()
    }
}
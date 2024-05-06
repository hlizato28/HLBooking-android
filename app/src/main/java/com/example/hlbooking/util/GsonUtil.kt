package com.example.hlbooking.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object GsonUtil {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer { src: LocalDate, _: Type?, _: JsonSerializationContext? ->
            JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
        })
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer { src: LocalTime, _: Type?, _: JsonSerializationContext? ->
            JsonPrimitive(src.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
            val jsonArray = json.asJsonArray
            LocalDate.of(jsonArray.get(0).asInt, jsonArray.get(1).asInt, jsonArray.get(2).asInt)
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer { json, _, _ ->
            val jsonArray = json.asJsonArray
            LocalTime.of(jsonArray.get(0).asInt, jsonArray.get(1).asInt)
        })
        .create()
}
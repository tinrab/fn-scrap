package com.flinect.scrap.common

import com.google.gson.GsonBuilder

object JsonUtil {
    val gson = GsonBuilder()
        .create()
    private val errorJson = FailureJson()

    fun encode(value: Any): String {
        return when (value) {
            is Failure -> gson.toJson(errorJson.serialize(value, null, null))
            else -> gson.toJson(value)
        }
    }

    inline fun <reified T : Any> decode(json: String): T {
        return gson.fromJson(json, T::class.java)
    }
}

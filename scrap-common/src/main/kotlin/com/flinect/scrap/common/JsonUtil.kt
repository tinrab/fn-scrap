package com.flinect.scrap.common

import com.google.gson.GsonBuilder

object JsonUtil {
    val gson = GsonBuilder()
        .create()
    private val serviceExceptionJson = ServiceExceptionJson()

    fun encode(value: Any): String {
        return when (value) {
            is ServiceException -> gson.toJson(serviceExceptionJson.serialize(value, null, null))
            else -> gson.toJson(value)
        }
    }

    inline fun <reified T : Any> decode(json: String): T {
        return gson.fromJson(json, T::class.java)
    }
}

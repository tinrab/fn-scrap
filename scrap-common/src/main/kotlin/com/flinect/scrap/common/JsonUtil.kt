package com.flinect.scrap.common

import com.google.gson.GsonBuilder
import java.io.Reader
import kotlin.reflect.KClass

object JsonUtil {
    private val gson = GsonBuilder()
        .create()
    private val errorJson = FailureJson()

    fun encode(value: Any): String {
        return when (value) {
            is Failure -> gson.toJson(errorJson.serialize(value, null, null))
            else -> gson.toJson(value)
        }
    }

    fun <T : Any> decode(json: String, clazz: KClass<T>): T {
        return gson.fromJson(json, clazz.java)
    }

    fun <T : Any> decode(reader: Reader, clazz: KClass<T>): T {
        return gson.fromJson(reader, clazz.java)
    }
}

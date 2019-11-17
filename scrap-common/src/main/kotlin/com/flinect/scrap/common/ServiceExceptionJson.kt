package com.flinect.scrap.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal class ServiceExceptionJson : JsonSerializer<ServiceException>, JsonDeserializer<ServiceException> {
    override fun serialize(src: ServiceException?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        requireNotNull(src)
        val jsonObject = JsonObject()
        jsonObject.addProperty("code", src.code)
        jsonObject.addProperty("message", src.message)
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ServiceException {
        return ServiceException("")
    }
}

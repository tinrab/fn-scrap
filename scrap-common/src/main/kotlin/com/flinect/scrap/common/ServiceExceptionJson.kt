package com.flinect.scrap.common

import com.google.gson.*
import java.lang.reflect.Type

internal class ServiceExceptionJson : JsonSerializer<ServiceException>, JsonDeserializer<ServiceException> {
    override fun serialize(src: ServiceException?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        requireNotNull(src)
        val jsonObject = JsonObject()
        jsonObject.addProperty("code", src.code)
        jsonObject.addProperty("kind", src.kind.name)
        if (src.message != null) {
            jsonObject.addProperty("message", src.message)
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ServiceException {
        require(json?.isJsonObject ?: false) { "Cannot deserialize a non-object." }
        val jsonObject = json?.asJsonObject as JsonObject

        println(json)

        return ServiceException(
            code = jsonObject["code"].asString,
            kind = ServiceException.Kind.valueOf(jsonObject["kind"].asString),
            message = jsonObject["message"].asString
        )
    }
}

package com.flinect.scrap.common

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class FailureJson : JsonSerializer<Failure> {
    override fun serialize(src: Failure?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("code", src.code)
        if (src.message != null) {
            jsonObject.addProperty("message", src.message)
        } else {
            jsonObject.addProperty("message", "Something bad happened.")
        }
        return jsonObject
    }
}

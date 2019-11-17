package com.flinect.scrap.message

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlin.reflect.KClass

/**
 * Utility class for working with JSON messages.
 *
 * A message has a type and payload:
 *
 * ```json
 * {
 *   "type": "ADD_TODO"
 *   "payload": {
 *     "text": "Write docs"
 *   }
 * }
 * ```
 */
class MessageJson<T : Any> private constructor(
    private val messageClass: KClass<*>,
    private val messageTypeAdapterFactory: MessageTypeAdapterFactory<T>,
    var gson: Gson
) {
    fun <S : T> toJson(value: S): String {
        require(messageTypeAdapterFactory.isRegistered(value)) {
            "Message type '${value::class}' was not registered."
        }
        return gson.toJson(value, messageClass.java)
    }

    fun fromJson(json: String): T {
        return gson.fromJson<T>(json, messageClass.java)
    }

    fun <T : Any> registerMessageType(clazz: KClass<T>) {
        messageTypeAdapterFactory.registerMessageType(clazz)
    }

    companion object {
        private const val TYPE_FIELD_NAME = "type"

        fun <T : Any> of(clazz: KClass<T>): MessageJson<T> {
            val messageTypeAdapterFactory = MessageTypeAdapterFactory<T>(clazz, TYPE_FIELD_NAME)
            val messageJson = MessageJson(
                clazz,
                messageTypeAdapterFactory,
                GsonBuilder()
                    .registerTypeAdapterFactory(messageTypeAdapterFactory)
                    .create()
            )
            for (k in clazz.annotations.filterIsInstance<MessageTypes>()
                .flatMap { messageTypes -> messageTypes.types.asList() }) {
                messageJson.registerMessageType(k)
            }
            return messageJson
        }
    }
}

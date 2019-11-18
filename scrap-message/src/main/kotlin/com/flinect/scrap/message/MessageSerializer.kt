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
class MessageSerializer<T : Message> private constructor(
    private val messageClass: KClass<*>,
    private val messageTypeAdapterFactory: MessageTypeAdapterFactory<T>,
    var gson: Gson
) {
    fun <S : T> encode(value: S): String {
        require(messageTypeAdapterFactory.isRegistered(value)) {
            "Message type '${value::class}' was not registered."
        }
        return gson.toJson(value, messageClass.java)
    }

    fun decode(json: String): T {
        return gson.fromJson<T>(json, messageClass.java)
    }

    fun <T : Any> registerMessageType(clazz: KClass<T>) {
        messageTypeAdapterFactory.registerMessageType(clazz)
    }

    companion object {
        private const val TYPE_FIELD_NAME = "type"

        fun <T : Message> of(clazz: KClass<T>): MessageSerializer<T> {
            val messageTypeAdapterFactory = MessageTypeAdapterFactory<T>(clazz, TYPE_FIELD_NAME)
            val messageSerializer = MessageSerializer(
                clazz,
                messageTypeAdapterFactory,
                GsonBuilder()
                    .registerTypeAdapterFactory(messageTypeAdapterFactory)
                    .create()
            )
            for (k in clazz.annotations.filterIsInstance<MessageTypes>()
                .flatMap { messageTypes -> messageTypes.types.asList() }) {
                messageSerializer.registerMessageType(k)
            }
            return messageSerializer
        }
    }
}

package com.flinect.scrap.message

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.Reader
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
class MessageSerializer private constructor(
    private val messageTypeAdapterFactory: MessageTypeAdapterFactory,
    var gson: Gson
) {
    init {
        registerMessageType(EmptyMessage::class)
    }

    fun <T : Message> encode(value: T): String {
        require(messageTypeAdapterFactory.isRegistered(value)) {
            "Message type '${value::class}' was not registered."
        }
        return gson.toJson(value, Message::class.java)
    }

    fun <T : Message> decode(json: String): T {
        val message = gson.fromJson<T>(json, Message::class.java)
        message.messageTypeName = Message.typeOf(message::class)
        return message
    }

    fun decode(reader: Reader): Message {
        return gson.fromJson<Message>(reader, Message::class.java)
    }

    fun <T : Any> registerMessageType(clazz: KClass<T>) {
        messageTypeAdapterFactory.registerMessageType(clazz)
    }

    companion object {
        fun <T : Message> of(clazz: KClass<T>): MessageSerializer {
            val messageTypeAdapterFactory = MessageTypeAdapterFactory()
            val messageSerializer = MessageSerializer(
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

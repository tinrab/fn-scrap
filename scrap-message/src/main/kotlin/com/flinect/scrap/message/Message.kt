package com.flinect.scrap.message

import kotlin.reflect.KClass

@MessageTypes(
    EmptyMessage::class
)
open class Message {
    internal var messageTypeName: String = typeOf(this::class)

    val type: String
        get() = messageTypeName

    companion object {
        fun <T : Message> typeOf(messageClass: KClass<T>): String {
            val messageTypeName = messageClass.annotations
                .filterIsInstance<MessageTypeName>()
                .firstOrNull()
            requireNotNull(messageTypeName) { "Message '${this::class}' has no MessageTypeName defined." }
            return messageTypeName.value
        }
    }
}

@MessageTypeName("builtin.empty")
class EmptyMessage : Message()

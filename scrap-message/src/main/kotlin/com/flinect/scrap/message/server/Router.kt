package com.flinect.scrap.message.server

import com.flinect.scrap.message.Message
import kotlin.reflect.KClass

class Router {
    private val routes: MutableMap<String, MessageHandler<Message>> = HashMap()

    fun <T : Message> on(messageClass: KClass<T>, handler: MessageHandler<T>) {
        @Suppress("UNCHECKED_CAST")
        routes[Message.typeOf(messageClass)] = handler as MessageHandler<Message>
    }

    fun <T : Message> getHandler(type: String): MessageHandler<T>? {
        return routes[type]
    }
}

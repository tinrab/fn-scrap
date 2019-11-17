package com.flinect.scrap.message

import java.io.Closeable

interface Broker<T : Message> : Closeable {
    fun publish(exchangeName: String, message: T)

    fun publishRouted(exchangeName: String, routingKey: String, message: T)

    fun subscribe(exchangeName: String, handler: (message: T) -> Boolean)

    fun subscribeRouted(exchangeName: String, routingKey: String, handler: (message: T) -> Boolean)
}

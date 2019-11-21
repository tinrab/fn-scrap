package com.flinect.scrap.message.broker

import com.flinect.scrap.message.Message
import com.flinect.scrap.message.MessageListener
import java.io.Closeable

interface Broker : Closeable {
    fun scheduleTask(queueName: String, message: Message)

    fun processTask(queueName: String, handler: MessageListener)

    fun publish(exchangeName: String, message: Message)

    fun publishRouted(exchangeName: String, routingKey: String, message: Message)

    fun subscribe(exchangeName: String, handler: MessageListener)

    fun subscribeRouted(exchangeName: String, routingKey: String, handler: MessageListener)
}

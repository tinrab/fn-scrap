package com.flinect.scrap.message

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.MessageProperties
import java.io.Closeable

internal class BrokerImpl<T : Message> constructor(
    private val exchanges: Map<String, BrokerExchange>,
    private val messageSerializer: MessageSerializer<T>,
    private val connection: Connection
) : Broker<T>, Closeable {
    override fun publish(exchangeName: String, message: T) {
        publishRouted(exchangeName, "", message)
    }

    override fun publishRouted(exchangeName: String, routingKey: String, message: T) {
        val exchange = getExchange(exchangeName)
        val data = messageSerializer.encode(message).toByteArray()

        exchange.channel.basicPublish(
            exchange.name,
            routingKey,
            createProperties(exchange.durability),
            data
        )
    }

    override fun subscribe(exchangeName: String, handler: (message: T) -> Boolean) {
        subscribeRouted(exchangeName, "", handler)
    }

    override fun subscribeRouted(
        exchangeName: String,
        routingKey: String,
        handler: (message: T) -> Boolean
    ) {
        val exchange = getExchange(exchangeName)
        val queueName = exchange.channel.queueDeclare().queue

        exchange.channel.queueBind(queueName, exchange.name, routingKey)

        val deliverCallback = DeliverCallback { consumerTag, message ->
            if (handler(messageSerializer.decode(String(message.body)))) {
                exchange.channel.basicAck(message.envelope.deliveryTag, false)
            }
        }

        val cancelCallback = CancelCallback { consumerTag ->
        }

        exchange.channel.basicConsume(queueName, false, deliverCallback, cancelCallback)
    }

    override fun close() {
        connection.close()
    }

    private fun getExchange(exchangeName: String): BrokerExchange {
        return exchanges.getOrElse(exchangeName, {
            throw IllegalArgumentException("Unknown exchange '$exchangeName'.")
        })
    }

    private fun createProperties(durability: ExchangeDurability): AMQP.BasicProperties? {
        return if (durability != ExchangeDurability.WEAK) {
            MessageProperties.PERSISTENT_TEXT_PLAIN
        } else {
            MessageProperties.BASIC
        }.builder()
            .contentType("application/json")
            .build()
    }
}

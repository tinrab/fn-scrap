package com.flinect.scrap.message.broker

import com.flinect.scrap.message.Message
import com.flinect.scrap.message.MessageListener
import com.flinect.scrap.message.MessageSerializer
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Connection
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.MessageProperties
import java.io.Closeable

internal class BrokerImpl constructor(
    private val queues: Map<String, BrokerQueue>,
    private val exchanges: Map<String, BrokerExchange>,
    private val messageSerializer: MessageSerializer,
    private val connection: Connection
) : Broker, Closeable {
    override fun scheduleTask(queueName: String, message: Message) {
        val queue = getQueue(queueName)
        val data = messageSerializer.encode(message).toByteArray()

        queue.channel.basicPublish(
            "",
            queue.name,
            MessageProperties.PERSISTENT_TEXT_PLAIN.builder()
                .contentType("application/json")
                .build(),
            data
        )
    }

    override fun processTask(queueName: String, handler: MessageListener) {
        val queue = getQueue(queueName)

        val deliverCallback = DeliverCallback { _, message ->
            if (handler(messageSerializer.decode(String(message.body)))) {
                queue.channel.basicAck(message.envelope.deliveryTag, false)
            }
        }

        val cancelCallback = CancelCallback { }

        queue.channel.basicConsume(queueName, false, deliverCallback, cancelCallback)
    }

    override fun publish(exchangeName: String, message: Message) {
        publishRouted(exchangeName, "", message)
    }

    override fun publishRouted(exchangeName: String, routingKey: String, message: Message) {
        val exchange = getExchange(exchangeName)
        val data = messageSerializer.encode(message).toByteArray()

        exchange.channel.basicPublish(
            exchange.name,
            routingKey,
            createProperties(exchange.durability),
            data
        )
    }

    override fun subscribe(exchangeName: String, handler: MessageListener) {
        subscribeRouted(exchangeName, "", handler)
    }

    override fun subscribeRouted(
        exchangeName: String,
        routingKey: String,
        handler: MessageListener
    ) {
        val exchange = getExchange(exchangeName)
        val queueName = exchange.channel.queueDeclare().queue

        exchange.channel.queueBind(queueName, exchange.name, routingKey)

        val deliverCallback = DeliverCallback { _, message ->
            if (handler(messageSerializer.decode(String(message.body)))) {
                exchange.channel.basicAck(message.envelope.deliveryTag, false)
            }
        }

        val cancelCallback = CancelCallback { }

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

    private fun getQueue(queueName: String): BrokerQueue {
        return queues.getOrElse(queueName, {
            throw IllegalArgumentException("Unknown exchange '$queueName'.")
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

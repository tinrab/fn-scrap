package com.flinect.scrap.message.broker

import com.flinect.scrap.message.MessageSerializer
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class BrokerBuilder private constructor(
    private val messageSerializer: MessageSerializer,
    private val connection: Connection
) {
    private val exchanges = HashMap<String, BrokerExchange>()
    private val queues = HashMap<String, BrokerQueue>()

    fun addQueue(name: String, prefetchCount: Int? = null): BrokerBuilder {
        require(!queues.containsKey(name)) { "Queue name must be unique." }

        val channel = connection.createChannel()
        channel.queueDeclare(name, true, true, true, null)
        if (prefetchCount != null) {
            channel.basicQos(prefetchCount, true)
        }

        queues[name] = BrokerQueue(
            name,
            channel
        )
        return this
    }

    fun addExchange(
        name: String,
        type: ExchangeType,
        durability: ExchangeDurability,
        prefetchCount: Int? = null
    ): BrokerBuilder {
        require(!exchanges.containsKey(name)) { "Exchange name must be unique." }
        val channel = connection.createChannel()

        if (prefetchCount != null) {
            channel.basicQos(prefetchCount, true)
        }

        channel.exchangeDeclare(
            name,
            type.toAmqp(),
            durability != ExchangeDurability.WEAK
        )

        exchanges[name] = BrokerExchange(
            name,
            type.toAmqp(),
            durability,
            channel
        )

        return this
    }

    fun build(): Broker {
        return BrokerImpl(
            queues,
            exchanges,
            messageSerializer,
            connection
        )
    }

    companion object {
        fun of(config: BrokerConfig, messageSerializer: MessageSerializer): BrokerBuilder {
            val connectionFactory = ConnectionFactory()
            connectionFactory.host = config.host
            connectionFactory.port = config.port
            connectionFactory.isAutomaticRecoveryEnabled = true
            connectionFactory.username = config.username
            connectionFactory.password = config.password

            val connection = connectionFactory.newConnection()

            return BrokerBuilder(messageSerializer, connection)
        }
    }
}

package com.flinect.scrap.message

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import kotlin.reflect.KClass

class BrokerBuilder<T : Message> private constructor(
    private val messageClass: KClass<T>,
    private val connection: Connection
) {
    private val exchanges = HashMap<String, BrokerExchange>()

    fun addExchange(
        name: String,
        type: ExchangeType,
        durability: ExchangeDurability,
        prefetchCount: Int? = null
    ): BrokerBuilder<T> {
        require(!exchanges.containsKey(name)) { "Exchange name must be unique" }
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

    fun build(): Broker<T> {
        return BrokerImpl(
            exchanges,
            MessageSerializer.of(messageClass),
            connection
        )
    }

    companion object {
        fun <T : Message> of(config: BrokerConfig, messageClass: KClass<T>): BrokerBuilder<T> {
            val connectionFactory = ConnectionFactory()
            connectionFactory.host = config.host
            connectionFactory.port = config.port
            connectionFactory.isAutomaticRecoveryEnabled = true
            connectionFactory.username = config.username
            connectionFactory.password = config.password

            val connection = connectionFactory.newConnection()

            return BrokerBuilder(messageClass, connection)
        }
    }
}

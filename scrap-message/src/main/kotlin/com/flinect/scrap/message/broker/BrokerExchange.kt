package com.flinect.scrap.message.broker

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel

internal data class BrokerExchange(
    val name: String,
    val type: BuiltinExchangeType,
    val durability: ExchangeDurability,
    val channel: Channel
)

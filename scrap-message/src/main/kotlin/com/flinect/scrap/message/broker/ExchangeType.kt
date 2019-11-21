package com.flinect.scrap.message.broker

import com.rabbitmq.client.BuiltinExchangeType

enum class ExchangeType {
    FANOUT,
    DIRECT,
    TOPIC;

    internal fun toAmqp(): BuiltinExchangeType {
        return when (this) {
            FANOUT -> BuiltinExchangeType.FANOUT
            DIRECT -> BuiltinExchangeType.DIRECT
            TOPIC -> BuiltinExchangeType.TOPIC
        }
    }
}

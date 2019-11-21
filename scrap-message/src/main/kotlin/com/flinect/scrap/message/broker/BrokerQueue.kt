package com.flinect.scrap.message.broker

import com.rabbitmq.client.Channel

internal class BrokerQueue(
    val name: String,
    val channel: Channel
)

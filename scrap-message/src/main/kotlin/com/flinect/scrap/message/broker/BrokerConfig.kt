package com.flinect.scrap.message.broker

data class BrokerConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String
)

package com.flinect.scrap.message

data class BrokerConfig(
    val host: String,
    val port: Int,
    val username: String = "guest",
    val password: String = "guest"
)

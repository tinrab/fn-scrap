package com.flinect.scrap.message.client

import java.time.Duration

data class ClientConfig(
    val host: String,
    val port: Int,
    val timeout: Duration = Duration.ofSeconds(5)
)

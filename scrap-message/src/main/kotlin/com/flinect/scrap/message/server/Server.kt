package com.flinect.scrap.message.server

import com.flinect.scrap.message.MessageSerializer

interface Server {
    fun start()

    fun stop()

    companion object {
        fun create(serverConfig: ServerConfig, router: Router, messageSerializer: MessageSerializer): Server {
            return ServerImpl(serverConfig, router, messageSerializer)
        }
    }
}

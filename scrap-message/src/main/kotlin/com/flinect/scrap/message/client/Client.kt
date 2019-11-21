package com.flinect.scrap.message.client

import com.flinect.scrap.message.Message
import com.flinect.scrap.message.MessageSerializer
import org.eclipse.jetty.client.api.ContentResponse

interface Client {
    fun start()

    fun stop()

    fun ping(): ContentResponse

    fun <T : Message> call(request: T): CallResponse

    companion object {
        fun of(clientConfig: ClientConfig, messageSerializer: MessageSerializer): Client {
            return ClientImpl(clientConfig, messageSerializer)
        }
    }
}

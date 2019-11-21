package com.flinect.scrap.message.client

import com.flinect.scrap.common.ByteLengthUtil
import com.flinect.scrap.message.Message
import com.flinect.scrap.message.MessageSerializer
import io.javalin.plugin.openapi.annotations.ContentType
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.ContentResponse
import org.eclipse.jetty.client.util.FutureResponseListener
import org.eclipse.jetty.client.util.StringContentProvider
import org.eclipse.jetty.http.HttpMethod
import java.util.concurrent.TimeUnit

internal class ClientImpl(
    private val clientConfig: ClientConfig,
    private val messageSerializer: MessageSerializer
) : Client {
    private lateinit var httpClient: HttpClient

    override fun start() {
        httpClient = HttpClient()
        httpClient.start()
    }

    override fun stop() {
        httpClient.stop()
    }

    override fun ping(): ContentResponse {
        return httpClient.newRequest(clientConfig.host, clientConfig.port)
            .method(HttpMethod.GET)
            .path(PING_PATH)
            .send()
    }

    override fun <T : Message> call(request: T): CallResponse {
        val req = httpClient.newRequest(clientConfig.host, clientConfig.port)
            .method(HttpMethod.POST)
            .path(MESSAGES_PATH)
            .content(StringContentProvider(messageSerializer.encode(request)), ContentType.JSON)
        val responseListener = FutureResponseListener(req, MAX_RESPONSE_LENGTH)
        req.send(responseListener)
        val res = responseListener.get(clientConfig.timeout.toMillis(), TimeUnit.MILLISECONDS)
        val message = messageSerializer.decode<Message>(res.contentAsString)
        return CallResponse(
            res,
            message
        )
    }

    companion object {
        private const val PING_PATH = "/ping"
        private const val MESSAGES_PATH = "/messages"
        private val MAX_RESPONSE_LENGTH = ByteLengthUtil.kb(512).toInt()
    }
}

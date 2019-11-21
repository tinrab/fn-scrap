package com.flinect.scrap.message

import com.flinect.scrap.message.client.Client
import com.flinect.scrap.message.client.ClientConfig
import com.flinect.scrap.message.server.Router
import com.flinect.scrap.message.server.Server
import com.flinect.scrap.message.server.ServerConfig
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Tag("integration")
class ServerClientTest {
    @MessageTypes(
        EchoRequest::class,
        EchoResponse::class
    )
    private open class Action : Message()

    @MessageTypeName("echo.request")
    private data class EchoRequest(
        val text: String
    ) : Action()

    @MessageTypeName("echo.response")
    private data class EchoResponse(
        val text: String
    ) : Action()

    @Test
    @Timeout(3)
    fun basic() {
        val messageSerializer = MessageSerializer.of(Action::class)

        val router = Router()
        router.on(EchoRequest::class) { message, _ ->
            EchoResponse(message.text)
        }
        val server = Server.create(ServerConfig(9000), router, messageSerializer)
        server.start()
        Thread.sleep(500)

        val client = Client.of(ClientConfig("localhost", 9000), messageSerializer)
        client.start()

        val pingRes = client.ping()
        assertEquals(HttpStatus.OK_200, pingRes.status)

        val echoRes = client.call(EchoRequest("hello"))
        assertEquals(HttpStatus.OK_200, echoRes.response.status)
        assert(echoRes.message is EchoResponse)
        assertEquals("hello", (echoRes.message as EchoResponse).text)
        client.stop()
    }
}

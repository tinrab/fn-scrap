package com.flinect.scrap.message.server

import com.flinect.scrap.common.Failure
import com.flinect.scrap.common.JsonUtil
import com.flinect.scrap.common.ServiceException
import com.flinect.scrap.message.Message
import com.flinect.scrap.message.MessageSerializer
import io.javalin.Javalin
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus

internal class ServerImpl(
    private val serverConfig: ServerConfig,
    private val router: Router,
    private val messageSerializer: MessageSerializer
) : Server {
    private lateinit var app: Javalin

    override fun start() {
        // TODO: config
        app = Javalin.create { }
        app.get("/ping", this::handlePing)
        app.post("/messages", this::handleMessages)
        app.exception(Exception::class.java) { e, ctx ->
            if (e is ServiceException) {
                ctx.status(e.httpStatus)
                    .result(JsonUtil.encode(Failure(e.code, e.message)))
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
                    .result(JsonUtil.encode(ServerException.internal("Unknown error.")))
            }
        }
        app.start(serverConfig.port)
    }

    override fun stop() {
        app.stop()
    }

    private fun handlePing(ctx: Context) {
        ctx.result("PONG")
    }

    private fun handleMessages(ctx: Context) {
        try {
            val message = messageSerializer.decode<Message>(ctx.body())
            val handler =
                router.getHandler<Message>(message.type) ?: throw ServerException.badRequest("Unknown message.")

            val result = handler(message, ctx.headerMap())

            ctx.result(messageSerializer.encode(result))
        } catch (e: Exception) {
            e.printStackTrace()
            throw ServerException.badRequest("Invalid payload.")
        }
    }
}

package com.flinect.scrap.message.client

import com.flinect.scrap.message.Message
import org.eclipse.jetty.client.api.Response

class CallResponse(
    val response: Response,
    val message: Message
)

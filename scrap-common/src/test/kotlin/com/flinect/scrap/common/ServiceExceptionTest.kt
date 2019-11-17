package com.flinect.scrap.common

import org.junit.Test
import kotlin.test.assertEquals

class ServiceExceptionTest {
    @Test
    fun basic() {
        class TestServiceException(code: String, message: String?) : ServiceException(
            code,
            message = message,
            kind = Kind.BAD_REQUEST
        )

        val e = TestServiceException("code", message = "msg")
        val json = JsonUtil.encode(e)
        assertEquals("{\"code\":\"code\",\"kind\":\"BAD_REQUEST\",\"message\":\"msg\"}", json)

        val decoded = JsonUtil.decode(json) as ServiceException
        assertEquals(e.code, decoded.code)
        assertEquals(e.kind, decoded.kind)
        assertEquals(e.message, decoded.message)
    }
}

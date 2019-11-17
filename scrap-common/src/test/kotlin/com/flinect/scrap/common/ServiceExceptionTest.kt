package com.flinect.scrap.common

import org.junit.Test
import kotlin.test.assertEquals

class ServiceExceptionTest {
    @Test
    fun basic() {
        class TestServiceException(code: String, message: String) : ServiceException(
            code,
            message = message,
            kind = Kind.BAD_REQUEST
        )

        val e = TestServiceException("code", message = "msg")
        assertEquals("{\"code\":\"code\",\"message\":\"msg\"}", JsonUtil.encodeToString(e))
    }
}

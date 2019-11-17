package com.flinect.scrap.common

import org.junit.Test
import kotlin.test.assertEquals

class LongUtilTest {
    @Test
    fun values() {
        assertEquals("42", LongUtil.toUnsignedBigInteger(42).toString())
        assertEquals("9223372036854775807", LongUtil.toUnsignedBigInteger(Long.MAX_VALUE).toString())
        assertEquals("9223372036854775808", LongUtil.toUnsignedBigInteger(Long.MIN_VALUE).toString())
        assertEquals("18446744073709551615", LongUtil.toUnsignedBigInteger(-1).toString())
    }
}

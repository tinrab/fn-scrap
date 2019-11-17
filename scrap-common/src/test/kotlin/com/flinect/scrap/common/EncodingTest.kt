package com.flinect.scrap.common

import org.junit.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals
import kotlin.test.fail

class EncodingTest {
    @Test
    fun encodeBase62ToString() {
        val buffer = ByteBuffer.allocate(8)
        buffer.putLong(1337)
        assertEquals("LZ", Encoding.encodeBase62String(buffer.array()))

        assertEquals("QmIN", Encoding.encodeBase62String("abc".toByteArray()))

        assertEquals("", Encoding.encodeBase62String(ByteArray(0)))
    }

    @Test
    fun decodeBase64String() {
        assertEquals("abc".toByteArray().toList(), Encoding.decodeBase62String("QmIN").toList())
    }

    @Test
    fun validation() {
        val cases = listOf(
            { Encoding.convert(byteArrayOf(), 0, 0) },
            { Encoding.convert(byteArrayOf(), -1, -1) },
            { Encoding.convert(byteArrayOf(), 64, -1) }
        )
        for (f in cases) {
            try {
                f()
                fail("no IllegalStateException thrown")
            } catch (e: IllegalStateException) {
            }
        }
    }
}

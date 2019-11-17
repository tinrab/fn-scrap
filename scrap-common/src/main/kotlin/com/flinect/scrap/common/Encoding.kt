package com.flinect.scrap.common

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.ceil
import kotlin.math.ln

object Encoding {
    private const val BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private val BASE62_REVERSE_MAP by lazy {
        BASE62_ALPHABET
            .mapIndexed { index, c -> c to index }
            .toMap()
    }

    /**
     * Encodes [data] to Base62 [ByteArray].
     */
    fun encodeBase62(data: ByteArray): ByteArray {
        return convert(data, 256, 62)
    }

    /**
     * Decodes Base62 [data] to [ByteArray].
     */
    fun decodeBase62(data: ByteArray): ByteArray {
        return convert(data, 62, 256)
    }

    /**
     * Encodes [data] to Base62 [String].
     */
    fun encodeBase62String(data: ByteArray): String {
        return String(
            encodeBase62(data)
                .map { BASE62_ALPHABET[it.toInt()] }
                .toCharArray()
        )
    }

    /**
     * Decodes Base62 [value] to [ByteArray].
     */
    fun decodeBase62String(value: String): ByteArray {
        val buffer = ByteBuffer.allocate(value.length)
        for (c in value) {
            check(BASE62_REVERSE_MAP.containsKey(c))
            buffer.put(BASE62_REVERSE_MAP.getValue(c).toByte())
        }
        return decodeBase62(buffer.array())
    }

    /**
     * Converts [data] from base [fromBase] to base [toBase].
     */
    fun convert(data: ByteArray, fromBase: Int, toBase: Int): ByteArray {
        check(fromBase != toBase) { "Source and target bases are equal." }
        check(fromBase >= 2) { "Invalid fromBase." }
        check(toBase >= 2) { "Invalid toBase." }

        val output = ByteArrayOutputStream(
            ceil((ln(fromBase.toDouble()) / ln(toBase.toDouble())) * data.size).toInt()
        )
        var src = data.copyOf()

        while (src.isNotEmpty()) {
            val quotient = ByteArrayOutputStream(src.size)
            var remainder = 0

            for (element in src) {
                val accumulator = (element.toInt() and 0xFF) + remainder * fromBase
                val digit = (accumulator - (accumulator % toBase)) / toBase
                remainder = accumulator % toBase
                if (quotient.size() > 0 || digit > 0) {
                    quotient.write(digit)
                }
            }

            output.write(remainder)
            src = quotient.toByteArray()
        }

        val r = output.toByteArray()
        r.reverse()
        return r
    }
}

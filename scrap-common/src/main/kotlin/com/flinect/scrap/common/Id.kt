package com.flinect.scrap.common

import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.Base64

data class Id(
    val value: Long
) {
    /**
     * Returns value of this id represented in Base62.
     */
    fun toBase62(): String {
        var x = LongUtil.toUnsignedBigInteger(value)
        val sb = StringBuilder()
        val len = BigInteger.valueOf(BASE62_ALPHABET.length.toLong())
        while (x > BigInteger.ZERO) {
            sb.insert(0, BASE62_ALPHABET.elementAt(x.remainder(len).toInt()))
            x = x.divide(len)
        }
        return sb.toString()
    }

    /**
     * Returns value of this id represented in Base64.
     */
    fun toBase64(): String {
        val buffer = ByteBuffer.allocate(Long.SIZE_BYTES)
        buffer.putLong(value)
        return Base64.getUrlEncoder().encodeToString(buffer.array())
    }

    override fun toString(): String {
        if (value >= 0L) {
            return value.toString()
        }
        return LongUtil.toUnsignedBigInteger(value).toString()
    }

    operator fun compareTo(other: Id): Int {
        return value.compareTo(other.value)
    }

    operator fun compareTo(other: Long): Int {
        return value.compareTo(other)
    }

    companion object {
        private const val BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        /**
         * Parses Base64 [value] to [Id].
         */
        fun fromBase64(value: String): Id {
            val buffer = ByteBuffer.wrap(Base64.getUrlDecoder().decode(value))
            return Id(buffer.long)
        }

        /**
         * Parses Base62 [value] to [Id].
         */
        fun fromBase62(value: String): Id {
            var id = 0L
            for (i in value.length - 1 downTo 0 step 1) {
                val c = value.elementAt(i)
                var j = -1
                for (k in BASE62_ALPHABET.indices) {
                    if (BASE62_ALPHABET.elementAt(k) == c) {
                        j = k
                        break
                    }
                }

                check(j != -1)

                var m = value.length - 1L - i
                var p = 1L
                while (m > 0) {
                    p *= BASE62_ALPHABET.length.toLong()
                    m--
                }
                id += j * p
            }
            return Id(id)
        }
    }
}

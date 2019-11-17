package com.flinect.scrap.common

import java.math.BigInteger

object LongUtil {
    /**
     * Returns unsigned representation of [value].
     */
    fun toUnsignedBigInteger(value: Long): BigInteger {
        if (value >= 0) {
            return BigInteger.valueOf(value)
        }
        return BigInteger.TWO.multiply(BigInteger.valueOf(Long.MAX_VALUE))
            .add(BigInteger.TWO)
            .add(BigInteger.valueOf(value))
    }
}

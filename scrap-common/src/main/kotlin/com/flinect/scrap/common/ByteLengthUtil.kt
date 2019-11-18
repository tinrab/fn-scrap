package com.flinect.scrap.common

object ByteLengthUtil {
    infix fun kb(value: Long): Long {
        return value * 1024
    }
}

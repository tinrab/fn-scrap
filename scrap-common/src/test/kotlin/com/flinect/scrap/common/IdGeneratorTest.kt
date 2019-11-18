package com.flinect.scrap.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant

@Tag("unit")
class IdGeneratorTest {
    @Test
    fun generate() {
        val g = IdGenerator.of(1, Instant.EPOCH)
        val id1 = g.generate()
        val id2 = g.generate()
        val id3 = g.generate()
        assert(id1 < id2)
        assert(id2 < id3)
    }

    @Test
    fun generateList() {
        val g = IdGenerator.of(1, Instant.EPOCH)
        val ids = g.generateList(5)
        assertEquals(5, ids.distinct().size)
    }
}

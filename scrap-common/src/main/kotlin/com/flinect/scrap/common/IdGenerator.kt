package com.flinect.scrap.common

import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class IdGenerator private constructor(
    private val workerId: Int
) {
    private var sequence: Int = 0
    private var startTime: Instant = Instant.now()
    private var lastTime: Instant = Instant.EPOCH

    /**
     * Generates new unique [Id].
     * Possibly sleeps to prevent duplicates due to timestamp.
     */
    fun generate(): Id {
        val now = Instant.now()
        lastTime = now.plus(Duration.between(startTime, now))

        val value = next()

        sequence++
        if (sequence >= 1 shl SEQUENCE_BIT_LENGTH) {
            sequence = 0
            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        }

        return value
    }

    /**
     * Generates a list of [Id]s.
     * Faster version of [generate] when generating multiple ids.
     */
    fun generateList(size: Int): List<Id> {
        var now = Instant.now()
        lastTime = now.plus(Duration.between(startTime, now))

        val list = ArrayList<Id>(size)

        for (i in 1..size) {
            list.add(next())

            sequence++
            if (sequence >= 1 shl SEQUENCE_BIT_LENGTH) {
                sequence = 0
                Thread.sleep(TimeUnit.SECONDS.toMillis(1))

                now = Instant.now()
                lastTime = now.plus(Duration.between(startTime, now))
            }
        }

        return list
    }

    private fun next(): Id {
        val t = lastTime.epochSecond
        val w = workerId.toLong()
        val s = sequence.toLong()

        check(t < 1L shl TIMESTAMP_BIT_LENGTH) { "Illegal time." }
        check(sequence < 1L shl SEQUENCE_BIT_LENGTH) { "Illegal sequence." }

        return Id(
            t and ((1L shl TIMESTAMP_BIT_LENGTH) - 1) shl (WORKER_BIT_LENGTH + SEQUENCE_BIT_LENGTH) or
                ((w and ((1L shl WORKER_BIT_LENGTH) - 1)) shl SEQUENCE_BIT_LENGTH) or
                (s and ((1L shl SEQUENCE_BIT_LENGTH) - 1))
        )
    }

    companion object {
        private const val TIMESTAMP_BIT_LENGTH = 37
        private const val WORKER_BIT_LENGTH = 16
        private const val SEQUENCE_BIT_LENGTH = 10

        fun of(workerId: Int, startTime: Instant): IdGenerator {
            return IdGenerator(workerId).also {
                it.startTime = startTime
            }
        }

        fun ofWorkerId(workerId: Int): IdGenerator {
            return IdGenerator(workerId)
        }
    }
}

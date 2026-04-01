package com.nevis.util

import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConcurrencyTest {

    @Test
    fun `runConcurrently returns results from both tasks`() {
        val (a, b) = runConcurrently(
            { listOf(1, 2) },
            { listOf("a", "b") },
        )

        assertEquals(listOf(1, 2), a)
        assertEquals(listOf("a", "b"), b)
    }

    @Test
    fun `runConcurrently actually runs concurrently`() {
        val duration = measureTimeMillis {
            runConcurrently(
                { Thread.sleep(200); 1 },
                { Thread.sleep(200); 2 },
            )
        }

        assertTrue(duration < 350, "Expected concurrent execution but took ${duration}ms")
    }

    @Test
    fun `mapConcurrently transforms all items`() {
        val results = listOf("a", "b", "c").mapConcurrently { it.uppercase() }

        assertEquals(listOf("A", "B", "C"), results)
    }

    @Test
    fun `mapConcurrently preserves order`() {
        val results = listOf(3, 1, 2).mapConcurrently {
            Thread.sleep(it * 50L)
            it * 10
        }

        assertEquals(listOf(30, 10, 20), results)
    }

    @Test
    fun `mapConcurrently on empty list returns empty list`() {
        val results = emptyList<Int>().mapConcurrently { it * 2 }

        assertEquals(emptyList(), results)
    }
}

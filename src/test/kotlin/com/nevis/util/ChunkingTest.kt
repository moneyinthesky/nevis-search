package com.nevis.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChunkingTest {

    @Test
    fun `short text returns a single chunk`() {
        val chunks = chunkText("Hello!", chunkSize = 10, overlap = 3)

        assertEquals(listOf("Hello!"), chunks)
    }

    @Test
    fun `text exactly at chunk size returns a single chunk`() {
        val chunks = chunkText("abcde", chunkSize = 5, overlap = 2)

        assertEquals(listOf("abcde"), chunks)
    }

    @Test
    fun `text longer than chunk size produces chunks with correct overlap`() {
        //                     abcde
        //                        defgh
        //                           ghij
        val chunks = chunkText("abcdefghij", chunkSize = 5, overlap = 2)

        assertEquals(listOf("abcde", "defgh", "ghij"), chunks)
    }

    @Test
    fun `all text is covered by chunks`() {
        val text = "abcdefghij".repeat(500) // 5000 chars
        val chunks = chunkText(text, chunkSize = 2000, overlap = 200)

        val reconstructed = buildString {
            for (i in chunks.indices) {
                if (i == 0) append(chunks[i])
                else append(chunks[i].drop(200))
            }
        }
        assertEquals(text, reconstructed)
    }

    @Test
    fun `empty text returns a single empty chunk`() {
        val chunks = chunkText("", chunkSize = 5, overlap = 2)

        assertEquals(listOf(""), chunks)
    }

    @Test
    fun `large document produces expected number of chunks`() {
        // stride = 2000 - 200 = 1800
        // starts: 0, 1800, 3600, 5400, 7200, 9000 (6 chunks)
        val chunks = chunkText("x".repeat(10000), chunkSize = 2000, overlap = 200)

        assertEquals(6, chunks.size)
        assertEquals(listOf(2000, 2000, 2000, 2000, 2000, 1000), chunks.map { it.length })
    }
}

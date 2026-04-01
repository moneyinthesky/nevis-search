package com.nevis.util

fun chunkText(text: String, chunkSize: Int = 500, overlap: Int = 100): List<String> {
    if (text.length <= chunkSize) return listOf(text)

    val stride = chunkSize - overlap
    return generateSequence(0) { start ->
        (start + stride).takeIf { it + overlap < text.length }
    }.map { start ->
        text.substring(start, (start + chunkSize).coerceAtMost(text.length))
    }.toList()
}

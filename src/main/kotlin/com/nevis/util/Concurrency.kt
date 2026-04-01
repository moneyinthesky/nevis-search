package com.nevis.util

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.Int.Companion.MAX_VALUE

fun <A, B> runConcurrently(taskA: () -> A, taskB: () -> B): Pair<A, B> = runBlocking {
    val a = async(IO) { taskA() }
    val b = async(IO) { taskB() }
    a.await() to b.await()
}

fun <T, R> List<T>.mapConcurrently(batchSize: Int = MAX_VALUE, transform: (T) -> R): List<R> = runBlocking {
    chunked(batchSize).flatMap { batch ->
        batch.map { async(IO) { transform(it) } }
            .awaitAll()
    }
}

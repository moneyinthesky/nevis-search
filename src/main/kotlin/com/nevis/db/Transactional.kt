package com.nevis.db

import org.jetbrains.exposed.sql.transactions.transaction

interface Transactional {
    fun <T> execute(block: () -> T): T
}

val ExposedTransactional = object : Transactional {
    override fun <T> execute(block: () -> T): T = transaction { block() }
}

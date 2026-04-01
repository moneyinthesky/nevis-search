package com.nevis.fakes

import com.nevis.db.Transactional

val NoOpTransactional = object : Transactional {
    override fun <T> execute(block: () -> T): T = block()
}

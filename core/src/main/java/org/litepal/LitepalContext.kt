package org.litepal

import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

internal val mutex = Mutex()

internal var dbSingleContextNullable: CoroutineContext? = null

val dbSingleContext: CoroutineContext
    get() = dbSingleContextNullable!!

val reentrantLock = ReentrantLock()
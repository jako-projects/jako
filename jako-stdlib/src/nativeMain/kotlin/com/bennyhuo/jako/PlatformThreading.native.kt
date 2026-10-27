package com.bennyhuo.jako

import java.lang.Thread

internal actual fun isMultiThreadingSupported(): Boolean = true

internal actual fun platformSleepSliceMillis(ms: Int) {
    if (ms > 0) {
        Thread.sleep(ms.toLong())
    }
}

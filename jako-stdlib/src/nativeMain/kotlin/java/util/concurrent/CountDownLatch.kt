package java.util.concurrent

import kotlin.time.TimeSource

actual open class CountDownLatch actual constructor(count: Int) {
    private var countVal = count

    init {
        require(count >= 0) { "count < 0" }
    }

    actual open fun await() {
        while (countVal > 0) {
        }
    }

    actual open fun await(timeout: Long, unit: TimeUnit): Boolean {
        val nanos = unit.toNanos(timeout)
        val mark = TimeSource.Monotonic.markNow()
        while (countVal > 0) {
            if (mark.elapsedNow().inWholeNanoseconds >= nanos) {
                return countVal == 0
            }
        }
        return true
    }

    actual open fun countDown() {
        if (countVal > 0) {
            countVal--
        }
    }

    actual open fun getCount(): Long = countVal.toLong()
}

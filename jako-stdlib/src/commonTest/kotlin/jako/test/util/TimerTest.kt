package jako.test.util

import java.lang.System
import java.lang.Thread
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimerTest {

    @Test
    fun testScheduleOnce() {
        val ran = AtomicInteger(0)
        val timer = Timer()
        try {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    ran.incrementAndGet()
                }
            }, 40L)
            waitUntil(2_000L) { ran.get() == 1 }
            assertEquals(1, ran.get())
        } finally {
            timer.cancel()
        }
    }

    @Test
    fun testScheduleWithPeriodThenCancel() {
        val ran = AtomicInteger(0)
        val timer = Timer(true)
        try {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    ran.incrementAndGet()
                }
            }, 20L, 40L)
            waitUntil(2_000L) { ran.get() >= 2 }
            assertTrue(ran.get() >= 2)
        } finally {
            timer.cancel()
        }
        val afterCancel = ran.get()
        Thread.sleep(80)
        assertEquals(afterCancel, ran.get())
    }
}

private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (!condition()) {
        if (System.currentTimeMillis() > deadline) {
            throw AssertionError("Condition not met within ${timeoutMs}ms")
        }
        Thread.sleep(10)
    }
}

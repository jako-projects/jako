package jako.test.lang

import java.lang.IllegalThreadStateException
import java.lang.InterruptedException
import java.lang.Thread
import java.lang.ThreadLocal
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ThreadTest {

    @Test
    fun testCurrentThreadIdentity() {
        val first = Thread.currentThread()
        val second = Thread.currentThread()
        assertTrue(first === second)
        assertTrue(first.isAlive)
        assertTrue(first.id > 0L)
    }

    @Test
    fun testStartRunsAsynchronously() {
        val started = AtomicInteger(0)
        val release = AtomicInteger(0)
        val ranOnId = AtomicLong(0)

        val thread = Thread {
            ranOnId.set(Thread.currentThread().id)
            started.set(1)
            waitUntil(2_000) { release.get() == 1 }
        }

        assertFalse(thread.isAlive)
        thread.start()
        waitUntil(2_000) { started.get() == 1 }
        assertTrue(thread.isAlive)
        assertEquals(thread.id, ranOnId.get())
        assertNotEquals(Thread.currentThread().id, thread.id)

        release.set(1)
        thread.join(2_000)
        assertFalse(thread.isAlive)
    }

    @Test
    fun testJoinWaitsForCompletion() {
        val counter = AtomicInteger(0)
        val thread = Thread {
            Thread.sleep(50)
            counter.set(42)
        }
        thread.start()
        thread.join()
        assertEquals(42, counter.get())
        assertFalse(thread.isAlive)
    }

    @Test
    fun testJoinTimeout() {
        val release = AtomicInteger(0)
        val thread = Thread {
            waitUntil(2_000) { release.get() == 1 }
        }
        thread.start()
        waitUntil(2_000) { thread.isAlive }
        thread.join(30)
        assertTrue(thread.isAlive)
        release.set(1)
        thread.join(2_000)
        assertFalse(thread.isAlive)
    }

    @Test
    fun testInterruptDuringSleep() {
        val entered = AtomicInteger(0)
        val sawInterrupt = AtomicInteger(0)
        val thread = Thread {
            try {
                entered.set(1)
                Thread.sleep(10_000)
            } catch (e: InterruptedException) {
                sawInterrupt.set(1)
            }
        }
        thread.start()
        waitUntil(2_000) { entered.get() == 1 }
        thread.interrupt()
        thread.join(2_000)
        assertEquals(1, sawInterrupt.get())
        assertFalse(thread.isInterrupted)
        assertFalse(thread.isAlive)
    }

    @Test
    fun testInterruptedStatus() {
        val thread = Thread.currentThread()
        thread.interrupt()
        try {
            assertTrue(thread.isInterrupted)
            assertTrue(thread.isInterrupted)
            assertTrue(Thread.interrupted())
            assertFalse(thread.isInterrupted)
            assertFalse(Thread.interrupted())
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun testStartTwiceThrows() {
        val thread = Thread {}
        thread.start()
        thread.join(2_000)
        assertFailsWith<IllegalThreadStateException> {
            thread.start()
        }
    }

    @Test
    fun testDefaultNameAndCustomName() {
        val named = Thread("worker-a")
        assertEquals("worker-a", named.name)
        named.name = "worker-b"
        assertEquals("worker-b", named.getName())

        val unnamed = Thread {}
        assertTrue(unnamed.name.startsWith("Thread-"))
    }

    @Test
    fun testDaemonFlagBeforeStart() {
        val thread = Thread {}
        assertFalse(thread.isDaemon)
        thread.isDaemon = true
        assertTrue(thread.isDaemon())
        thread.setDaemon(false)
        assertFalse(thread.isDaemon)
    }

    @Test
    fun testThreadLocalIsolation() {
        val local = ThreadLocal.withInitial { "default" }
        local.set("main")

        val childSawDefault = AtomicInteger(0)
        val thread = Thread {
            if (local.get() == "default") {
                childSawDefault.set(1)
            }
            local.set("child")
            assertEquals("child", local.get())
        }
        thread.start()
        thread.join(2_000)

        assertEquals(1, childSawDefault.get())
        assertEquals("main", local.get())
        local.remove()
        assertEquals("default", local.get())
    }

    @Test
    fun testNegativeSleepRejected() {
        assertFailsWith<IllegalArgumentException> {
            Thread.sleep(-1)
        }
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition()) {
            if (System.currentTimeMillis() >= deadline) {
                throw AssertionError("Timed out after ${timeoutMs}ms")
            }
            Thread.sleep(1)
        }
    }
}

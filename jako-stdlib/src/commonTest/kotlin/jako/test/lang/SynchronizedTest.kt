package jako.test.lang

import java.lang.Runnable
import java.lang.Thread
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.Synchronized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SynchronizedTest {

    private class MonitorHolder {
        var counter: Int = 0
        var overlapDetected: Boolean = false
        private var insideCriticalSection: Boolean = false

        @get:Synchronized
        @set:Synchronized
        var syncedProperty: Int = 0
            get() {
                assertTrue(Thread.holdsLock(this), "Getter must hold monitor lock")
                return field
            }
            set(value) {
                assertTrue(Thread.holdsLock(this), "Setter must hold monitor lock")
                field = value
            }

        @Synchronized
        fun verifyHoldsLock(): Boolean {
            return Thread.holdsLock(this)
        }

        @Synchronized
        fun outerReentrant(): Int {
            assertTrue(Thread.holdsLock(this), "Outer must hold lock before inner call")
            val v = innerReentrant()
            assertTrue(Thread.holdsLock(this), "Outer must still hold lock after inner call returns")
            return v + 1
        }

        @Synchronized
        fun innerReentrant(): Int {
            assertTrue(Thread.holdsLock(this), "Inner must hold lock")
            return 41
        }

        @Synchronized
        fun earlyReturn(branch: Int): String {
            assertTrue(Thread.holdsLock(this))
            if (branch == 1) {
                return "early-1"
            }
            if (branch == 2) {
                return "early-2"
            }
            return "normal"
        }

        @Synchronized
        fun throwingMethod() {
            assertTrue(Thread.holdsLock(this))
            throw IllegalArgumentException("boom")
        }

        @Synchronized
        fun incrementAndCheckOverlap() {
            assertTrue(Thread.holdsLock(this))
            if (insideCriticalSection) {
                overlapDetected = true
            }
            insideCriticalSection = true
            val current = counter
            Thread.yield()
            counter = current + 1
            insideCriticalSection = false
        }
    }

    @Test
    fun testSynchronizedAcquiresAndReleasesMonitor() {
        val holder = MonitorHolder()
        assertFalse(Thread.holdsLock(holder), "Lock must not be held before call")
        assertTrue(holder.verifyHoldsLock(), "Lock must be held inside @Synchronized method")
        assertFalse(Thread.holdsLock(holder), "Lock must be released after call")
    }

    @Test
    fun testSynchronizedReentrancy() {
        val holder = MonitorHolder()
        assertFalse(Thread.holdsLock(holder))
        assertEquals(42, holder.outerReentrant())
        assertFalse(Thread.holdsLock(holder))
    }

    @Test
    fun testSynchronizedEarlyReturnAndExceptionReleaseLock() {
        val holder = MonitorHolder()
        assertEquals("early-1", holder.earlyReturn(1))
        assertFalse(Thread.holdsLock(holder), "Lock must be released after early return 1")

        assertEquals("early-2", holder.earlyReturn(2))
        assertFalse(Thread.holdsLock(holder), "Lock must be released after early return 2")

        assertEquals("normal", holder.earlyReturn(3))
        assertFalse(Thread.holdsLock(holder), "Lock must be released after normal return")

        assertFailsWith<IllegalArgumentException> {
            holder.throwingMethod()
        }
        assertFalse(Thread.holdsLock(holder), "Lock must be released after exception")
    }

    @Test
    fun testSynchronizedPropertyAccessors() {
        val holder = MonitorHolder()
        assertFalse(Thread.holdsLock(holder))
        holder.syncedProperty = 99
        assertFalse(Thread.holdsLock(holder))
        assertEquals(99, holder.syncedProperty)
        assertFalse(Thread.holdsLock(holder))
    }

    @Test
    fun testMultiThreadedMutualExclusionAndInteroperabilityWithSynchronizedBlock() {
        val holder = MonitorHolder()
        val numThreads = 4
        val iterationsPerThread = 250
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(numThreads)
        val failed = AtomicBoolean(false)
        val syncedBlockCount = AtomicInteger(0)

        val threads = (0 until numThreads).map { idx ->
            Thread(Runnable {
                try {
                    startLatch.await(5, TimeUnit.SECONDS)
                    for (i in 0 until iterationsPerThread) {
                        if ((i + idx) % 5 == 0) {
                            // Verify @Synchronized shares the exact same monitor as synchronized(holder) { ... }
                            synchronized(holder) {
                                if (!Thread.holdsLock(holder)) {
                                    failed.set(true)
                                }
                                holder.incrementAndCheckOverlap()
                                syncedBlockCount.incrementAndGet()
                            }
                        } else {
                            holder.incrementAndCheckOverlap()
                        }
                    }
                } catch (t: Throwable) {
                    failed.set(true)
                } finally {
                    doneLatch.countDown()
                }
            })
        }

        startLatch.countDown()
        for (t in threads) {
            try {
                t.start()
            } catch (_: UnsupportedOperationException) {
                t.run()
            }
        }
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "Worker threads timed out")
        for (t in threads) {
            if (t.isAlive) t.join()
        }

        assertFalse(failed.get(), "A worker thread encountered an error")
        assertFalse(holder.overlapDetected, "Critical section overlap detected in @Synchronized")
        assertEquals(numThreads * iterationsPerThread, holder.counter)
        assertTrue(syncedBlockCount.get() > 0)
    }
}

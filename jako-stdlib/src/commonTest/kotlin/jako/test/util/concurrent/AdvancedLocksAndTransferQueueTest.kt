package jako.test.util.concurrent

import java.lang.Runnable
import java.lang.Thread
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport
import java.util.concurrent.locks.StampedLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdvancedLocksAndTransferQueueTest {

    @Test
    fun testLockSupport() {
        val current = Thread.currentThread()
        // Pre-unpark permit should allow next park() to return immediately
        LockSupport.unpark(current)
        LockSupport.park("pre-permit")
        assertNull(LockSupport.getBlocker(current))

        val parkedLatch = CountDownLatch(1)
        val unparkedFlag = AtomicBoolean(false)
        val blockerObj = "custom-blocker"

        val worker = Thread(Runnable {
            parkedLatch.countDown()
            LockSupport.park(blockerObj)
            unparkedFlag.set(true)
        })
        worker.start()
        assertTrue(parkedLatch.await(5, TimeUnit.SECONDS))
        Thread.sleep(30)
        LockSupport.unpark(worker)
        worker.join(5000)
        assertTrue(unparkedFlag.get())
    }

    @Test
    fun testStampedLock() {
        val sl = StampedLock()
        val optStamp = sl.tryOptimisticRead()
        assertTrue(optStamp != 0L)
        assertTrue(sl.validate(optStamp))

        val wStamp = sl.writeLock()
        assertTrue(sl.isWriteLocked)
        assertFalse(sl.validate(optStamp))

        val rStamp = sl.tryConvertToReadLock(wStamp)
        assertTrue(rStamp != 0L)
        assertFalse(sl.isWriteLocked)
        assertTrue(sl.isReadLocked)
        assertEquals(1, sl.readLockCount)

        val wStamp2 = sl.tryConvertToWriteLock(rStamp)
        assertTrue(wStamp2 != 0L)
        assertTrue(sl.isWriteLocked)
        sl.unlockWrite(wStamp2)
        assertFalse(sl.isWriteLocked)
        assertFalse(sl.isReadLocked)

        val rwView = sl.asReadWriteLock()
        rwView.readLock().lock()
        assertTrue(sl.isReadLocked)
        rwView.readLock().unlock()
        assertFalse(sl.isReadLocked)
    }

    @Test
    fun testLinkedTransferQueue() {
        val q = LinkedTransferQueue<String>()
        assertFalse(q.tryTransfer("dropped"))
        assertTrue(q.isEmpty())

        q.offer("a")
        q.put("b")
        assertEquals(2, q.size)
        assertEquals("a", q.poll())
        assertEquals("b", q.take())

        val consumerReady = CountDownLatch(1)
        val received = AtomicReference<String?>()
        val consumer = Thread(Runnable {
            consumerReady.countDown()
            received.set(q.take())
        })
        consumer.start()
        assertTrue(consumerReady.await(5, TimeUnit.SECONDS))
        Thread.sleep(30)
        q.transfer("direct-handoff")
        consumer.join(5000)
        assertEquals("direct-handoff", received.get())
    }

    @Test
    fun testAbstractExecutorServiceAndCompletionService() {
        val pool = Executors.newFixedThreadPool(2)
        try {
            val tasks = listOf(
                Callable { 10 },
                Callable { 20 },
                Callable { 30 }
            )
            val futures = pool.invokeAll(tasks)
            assertEquals(listOf(10, 20, 30), futures.map { it.get() })

            val anyResult = pool.invokeAny(tasks)
            assertTrue(anyResult in setOf(10, 20, 30))

            val ecs = ExecutorCompletionService<String>(pool)
            ecs.submit(Callable { "task-1" })
            ecs.submit(Runnable {}, "task-2")

            val r1 = ecs.take().get()
            val r2 = ecs.take().get()
            assertEquals(setOf("task-1", "task-2"), setOf(r1, r2))
            assertNull(ecs.poll())
        } finally {
            pool.shutdown()
        }
    }
}

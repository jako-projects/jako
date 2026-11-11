package jako.test.util.concurrent

import java.lang.Runnable
import java.lang.Thread
import java.util.AbstractQueue
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConcurrentCollectionsTest {

    @Test
    fun testConcurrentHashMapAsMutableMap() {
        val map = ConcurrentHashMap<String, Int>(8)
        assertNull(map.put("a", 1))
        assertEquals(1, map["a"])
        assertEquals(1, map.size)
        assertTrue(map.containsKey("a"))
        assertEquals(1, map.getOrDefault("a", 99))
        assertEquals(99, map.getOrDefault("missing", 99))

        assertEquals(1, map.putIfAbsent("a", 2))
        assertNull(map.putIfAbsent("b", 2))
        assertEquals(2, map["b"])

        assertFalse(map.replace("b", 999, 3))
        assertTrue(map.replace("b", 2, 3))
        assertEquals(3, map.replace("b", 4))
        assertNull(map.replace("missing", 4))

        assertEquals(10, map.computeIfAbsent("c") { k -> k.length * 10 })
        assertEquals(10, map.computeIfAbsent("c") { 999 })
        assertEquals(15, map.computeIfPresent("c") { _, v -> v + 5 })
        assertNull(map.computeIfPresent("c") { _, _ -> null })
        assertFalse(map.containsKey("c"))

        assertEquals(7, map.compute("d") { _, old -> (old ?: 0) + 7 })
        assertEquals(14, map.compute("d") { _, old -> (old ?: 0) + 7 })
        assertEquals(20, map.merge("d", 6) { a, b -> a + b })
        assertEquals(5, map.merge("e", 5) { a, b -> a + b })

        assertFalse(map.remove("e", 999))
        assertTrue(map.remove("e", 5))

        val copied = ConcurrentHashMap(map)
        assertEquals(map.size, copied.size)
        assertEquals(1, copied["a"])

        assertEquals(1, map.remove("a"))
        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun testCopyOnWriteArrayList() {
        val list = CopyOnWriteArrayList<String>()
        list.add("A")
        list.add("B")
        assertEquals(2, list.size)
        assertEquals("A", list[0])
        assertEquals("B", list[1])

        val iterator = list.iterator()
        list.add("C") // Snapshot iterator won't see C
        val iterated = mutableListOf<String>()
        while (iterator.hasNext()) {
            iterated.add(iterator.next())
        }
        assertEquals(listOf("A", "B"), iterated)
        assertEquals(3, list.size)
    }

    @Test
    fun testConcurrentLinkedQueue() {
        val queue = ConcurrentLinkedQueue<String>()
        assertTrue(queue.offer("X"))
        assertTrue(queue.offer("Y"))
        assertEquals("X", queue.peek())
        assertEquals("X", queue.poll())
        assertEquals("Y", queue.poll())
        assertNull(queue.poll())
    }

    @Test
    fun testLinkedBlockingDeque() {
        val deque = LinkedBlockingDeque<Int>()
        deque.put(10)
        deque.put(20)
        assertEquals(10, deque.take())
        assertEquals(20, deque.take())
    }

    @Test
    fun testSynchronousQueue() {
        val queue = SynchronousQueue<String>()
        assertEquals(0, queue.size)
        assertNull(queue.peek())
        assertNull(queue.poll())
    }

    @Test
    fun testAbstractQueueAndLinkedBlockingQueue() {
        assertFailsWith<IllegalArgumentException> {
            LinkedBlockingQueue<Int>(0)
        }
        assertFailsWith<IllegalArgumentException> {
            LinkedBlockingQueue<Int>(-5)
        }

        val lbq = LinkedBlockingQueue<String>(2)
        val asAbstract: AbstractQueue<String> = lbq
        assertEquals(2, lbq.remainingCapacity())
        assertTrue(asAbstract.add("first"))
        assertTrue(lbq.offer("second"))
        assertEquals(0, lbq.remainingCapacity())
        assertFalse(lbq.offer("third"))
        assertFalse(lbq.offer("third", 5L, TimeUnit.MILLISECONDS))
        assertFailsWith<IllegalStateException> {
            asAbstract.add("third")
        }

        assertEquals("first", lbq.peek())
        assertEquals("first", lbq.take())
        assertEquals(1, lbq.remainingCapacity())
        lbq.put("third")
        assertEquals(0, lbq.remainingCapacity())

        assertFailsWith<IllegalArgumentException> {
            lbq.drainTo(lbq)
        }

        val drained = mutableListOf<String>()
        assertEquals(1, lbq.drainTo(drained, 1))
        assertEquals(listOf("second"), drained)
        assertEquals(1, lbq.drainTo(drained))
        assertEquals(listOf("second", "third"), drained)
        assertNull(lbq.poll(5L, TimeUnit.MILLISECONDS))

        lbq.offer("x")
        asAbstract.clear()
        assertTrue(lbq.isEmpty())
    }

    @Test
    fun testExecutorsThreadPoolAndScheduledThreadPool() {
        val executor = Executors.newSingleThreadExecutor()
        assertFalse(executor.isShutdown())
        executor.shutdown()
        assertTrue(executor.isShutdown())

        val createdCount = AtomicInteger(0)
        val factory = ThreadFactory { r ->
            createdCount.incrementAndGet()
            Thread(r, "custom-worker")
        }

        assertFailsWith<IllegalArgumentException> {
            ThreadPoolExecutor(-1, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue(), factory)
        }
        assertFailsWith<IllegalArgumentException> {
            ThreadPoolExecutor(2, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue(), factory)
        }

        val fiveArgTpe = ThreadPoolExecutor(1, 2, 10L, TimeUnit.MILLISECONDS, LinkedBlockingQueue())
        try {
            assertEquals(7, fiveArgTpe.submit(Callable { 3 + 4 }).get(5L, TimeUnit.SECONDS))
        } finally {
            fiveArgTpe.shutdown()
        }

        val tpe = ThreadPoolExecutor(
            1,
            2,
            10L,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue(),
            factory
        )
        val absExec: AbstractExecutorService = tpe
        try {
            val f1 = absExec.submit(Callable { 21 * 2 })
            assertEquals(42, f1.get(5L, TimeUnit.SECONDS))
            assertTrue(createdCount.get() >= 1)

            val f2 = absExec.submit(Runnable { }, "done")
            assertEquals("done", f2.get(5L, TimeUnit.SECONDS))

            val allFutures = absExec.invokeAll(
                listOf(Callable { "a" }, Callable { "b" }),
                5L,
                TimeUnit.SECONDS
            )
            assertEquals(listOf("a", "b"), allFutures.map { it.get() })

            val anyResult = absExec.invokeAny(
                listOf(Callable { 99 }),
                5L,
                TimeUnit.SECONDS
            )
            assertEquals(99, anyResult)

            assertFailsWith<IllegalArgumentException> {
                absExec.invokeAny(emptyList<Callable<Int>>())
            }
        } finally {
            tpe.shutdown()
            assertTrue(tpe.isShutdown())
            tpe.awaitTermination(2L, TimeUnit.SECONDS)
        }

        assertFailsWith<IllegalArgumentException> {
            ScheduledThreadPoolExecutor(-1)
        }

        val stpe = ScheduledThreadPoolExecutor(1, factory)
        try {
            val sfCallable = stpe.schedule(Callable { "scheduled-ok" }, 10L, TimeUnit.MILLISECONDS)
            assertEquals("scheduled-ok", sfCallable.get(5L, TimeUnit.SECONDS))

            val flag = AtomicInteger(0)
            val sfRun = stpe.schedule(Runnable { flag.set(1) }, 5L, TimeUnit.MILLISECONDS)
            assertNull(sfRun.get(5L, TimeUnit.SECONDS))
            assertEquals(1, flag.get())

            assertFailsWith<IllegalArgumentException> {
                stpe.scheduleAtFixedRate(Runnable { }, 0L, 0L, TimeUnit.MILLISECONDS)
            }
            assertFailsWith<IllegalArgumentException> {
                stpe.scheduleWithFixedDelay(Runnable { }, 0L, -1L, TimeUnit.MILLISECONDS)
            }

            val ticks = AtomicInteger(0)
            val periodic = stpe.scheduleAtFixedRate(
                Runnable { ticks.incrementAndGet() },
                0L,
                10L,
                TimeUnit.MILLISECONDS
            )
            val deadline = java.lang.System.currentTimeMillis() + 2000L
            while (ticks.get() < 2 && java.lang.System.currentTimeMillis() < deadline) {
                Thread.sleep(5L)
            }
            assertTrue(ticks.get() >= 1)
            assertTrue(periodic.cancel(true))
            assertTrue(periodic.isCancelled())
        } finally {
            stpe.shutdownNow()
            assertTrue(stpe.isShutdown())
        }
    }
}

package jako.test.util.concurrent

import java.lang.Runnable
import java.lang.Void
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ForkJoinTest {

    private class FibTask(private val n: Int) : RecursiveTask<Int>() {
        override fun compute(): Int {
            if (n <= 1) return n
            val f1 = FibTask(n - 1)
            f1.fork()
            val f2 = FibTask(n - 2)
            return f2.compute() + f1.join()
        }
    }

    private class DoubleArrayAction(
        private val arr: IntArray,
        private val lo: Int,
        private val hi: Int
    ) : RecursiveAction() {
        override fun compute() {
            if (hi - lo <= 4) {
                for (i in lo until hi) {
                    arr[i] *= 2
                }
            } else {
                val mid = (lo + hi) ushr 1
                invokeAll(
                    DoubleArrayAction(arr, lo, mid),
                    DoubleArrayAction(arr, mid, hi)
                )
            }
        }
    }

    private class SumCompleter(
        completer: CountedCompleter<*>?,
        private val arr: IntArray,
        private val lo: Int,
        private val hi: Int
    ) : CountedCompleter<Int>(completer) {
        private var left: SumCompleter? = null
        private var right: SumCompleter? = null
        var localResult: Int = 0

        override fun compute() {
            if (hi - lo <= 2) {
                var s = 0
                for (i in lo until hi) s += arr[i]
                localResult = s
                tryComplete()
            } else {
                val mid = (lo + hi) ushr 1
                addToPendingCount(2)
                val l = SumCompleter(this, arr, lo, mid)
                val r = SumCompleter(this, arr, mid, hi)
                left = l
                right = r
                l.fork()
                r.compute()
                tryComplete()
            }
        }

        override fun onCompletion(caller: CountedCompleter<*>) {
            val l = left
            val r = right
            if (l != null && r != null) {
                localResult = l.localResult + r.localResult
            }
        }

        override fun getRawResult(): Int = localResult
    }

    @Test
    fun testVoidType() {
        assertEquals("void", Void.TYPE.getName())
        assertTrue(Void.TYPE.isPrimitive())
    }

    @Test
    fun testRecursiveTaskAndForkJoinPool() {
        val pool = ForkJoinPool(2)
        try {
            assertEquals(2, pool.getParallelism())
            val res = pool.invoke(FibTask(10))
            assertEquals(55, res)

            // Test commonPool
            val commonRes = ForkJoinPool.commonPool().invoke(FibTask(8))
            assertEquals(21, commonRes)
            assertTrue(ForkJoinPool.getCommonPoolParallelism() >= 1)
        } finally {
            pool.shutdown()
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS))
            assertTrue(pool.isShutdown())
            assertTrue(pool.isTerminated())
        }
    }

    @Test
    fun testRecursiveAction() {
        val data = IntArray(16) { it + 1 }
        val action = DoubleArrayAction(data, 0, data.size)
        assertNull(ForkJoinPool.commonPool().invoke(action))
        assertTrue(action.isDone())
        assertTrue(action.isCompletedNormally())
        assertFalse(action.isCompletedAbnormally())
        for (i in data.indices) {
            assertEquals((i + 1) * 2, data[i])
        }
    }

    @Test
    fun testCountedCompleter() {
        val arr = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val root = SumCompleter(null, arr, 0, arr.size)
        assertSame(root, root.getRoot())
        assertNull(root.getCompleter())
        val sum = ForkJoinPool.commonPool().invoke(root)
        assertEquals(36, sum)
        assertEquals(0, root.getPendingCount())
        assertTrue(root.isCompletedNormally())
    }

    @Test
    fun testForkJoinTaskAdaptTagAndExceptions() {
        val callableTask = ForkJoinTask.adapt(Callable { "adapted-callable" })
        assertEquals(0.toShort(), callableTask.getForkJoinTaskTag())
        assertEquals(0.toShort(), callableTask.setForkJoinTaskTag(7))
        assertEquals(7.toShort(), callableTask.getForkJoinTaskTag())
        assertTrue(callableTask.compareAndSetForkJoinTaskTag(7, 9))
        assertFalse(callableTask.compareAndSetForkJoinTaskTag(7, 11))
        assertEquals(9.toShort(), callableTask.getForkJoinTaskTag())

        assertEquals("adapted-callable", ForkJoinPool.commonPool().invoke(callableTask))

        val runnableResultTask = ForkJoinTask.adapt(Runnable {}, 42)
        assertEquals(42, runnableResultTask.invoke())

        val failingTask = object : RecursiveTask<Int>() {
            override fun compute(): Int {
                throw IllegalStateException("boom")
            }
        }
        assertFailsWith<IllegalStateException> {
            failingTask.invoke()
        }
        assertTrue(failingTask.isCompletedAbnormally())
        assertFalse(failingTask.isCompletedNormally())
        assertNotNull(failingTask.getException())

        failingTask.reinitialize()
        assertFalse(failingTask.isDone())
        assertNull(failingTask.getException())
    }

    @Test
    fun testManagedBlockAndWorkStealingPool() {
        val counter = AtomicInteger(0)
        ForkJoinPool.managedBlock(object : ForkJoinPool.ManagedBlocker {
            override fun isReleasable(): Boolean = counter.get() >= 2
            override fun block(): Boolean {
                return counter.incrementAndGet() >= 2
            }
        })
        assertEquals(2, counter.get())

        val wsPool = Executors.newWorkStealingPool(2)
        try {
            val f = wsPool.submit(Callable { 123 })
            assertEquals(123, f.get(5, TimeUnit.SECONDS))
        } finally {
            wsPool.shutdown()
        }

        val single = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor())
        try {
            assertEquals("ok", single.submit(Callable { "ok" }).get())
        } finally {
            single.shutdown()
        }
    }
}

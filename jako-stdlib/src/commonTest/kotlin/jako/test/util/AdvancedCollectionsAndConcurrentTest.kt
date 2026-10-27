package jako.test.util

import java.lang.Runnable
import java.lang.System
import java.lang.Thread
import java.util.EnumMap
import java.util.EnumSet
import java.util.IdentityHashMap
import java.util.Objects
import java.util.PriorityQueue
import java.util.TreeSet
import java.util.WeakHashMap
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.Exchanger
import java.util.concurrent.Executors
import java.util.concurrent.Phaser
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.DoubleAdder
import java.util.concurrent.atomic.LongAdder
import java.util.function.Supplier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AdvancedCollectionsAndConcurrentTest {

    enum class Color { RED, GREEN, BLUE, YELLOW }

    @Test
    fun testObjectsDeepEqualsAndSupplier() {
        assertTrue(Objects.deepEquals(null, null))
        assertFalse(Objects.deepEquals("a", null))
        assertTrue(Objects.deepEquals(intArrayOf(1, 2, 3), intArrayOf(1, 2, 3)))
        assertFalse(Objects.deepEquals(intArrayOf(1, 2, 3), intArrayOf(1, 2, 4)))
        assertTrue(
            Objects.deepEquals(
                arrayOf<Any>(byteArrayOf(1, 2), arrayOf("x", "y")),
                arrayOf<Any>(byteArrayOf(1, 2), arrayOf("x", "y"))
            )
        )

        assertEquals("ok", Objects.requireNonNull("ok", Supplier { "should not fail" }))
        val ex = assertFailsWith<NullPointerException> {
            Objects.requireNonNull(null as String?, Supplier { "custom supplier message" })
        }
        assertEquals("custom supplier message", ex.message)
    }

    @Test
    fun testPriorityQueue() {
        val pq = PriorityQueue<Int>()
        pq.addAll(listOf(5, 1, 3, 2, 4, 1))
        assertEquals(6, pq.size)
        assertEquals(1, pq.peek())
        val drained = mutableListOf<Int>()
        while (pq.isNotEmpty()) {
            drained.add(pq.poll()!!)
        }
        assertEquals(listOf(1, 1, 2, 3, 4, 5), drained)
        assertNull(pq.poll())

        // Custom reverse comparator
        val maxPq = PriorityQueue<String>(Comparator { a, b -> b.compareTo(a) })
        maxPq.offer("apple")
        maxPq.offer("cherry")
        maxPq.offer("banana")
        assertNotNull(maxPq.comparator())
        assertEquals("cherry", maxPq.poll())
        assertEquals("banana", maxPq.poll())
        assertEquals("apple", maxPq.poll())

        // Construct from SortedSet
        val sorted = TreeSet<Int>()
        sorted.addAll(listOf(10, 30, 20))
        val fromSorted = PriorityQueue(sorted)
        assertEquals(10, fromSorted.poll())
        assertTrue(fromSorted.remove(30))
        assertEquals(20, fromSorted.poll())
    }

    @Test
    fun testIdentityHashMap() {
        val map = IdentityHashMap<List<String>?, Int?>()
        val k1 = ArrayList(listOf("key"))
        val k2 = ArrayList(listOf("key"))
        assertEquals(k1, k2)

        map[k1] = 100
        map[k2] = 200
        map[null] = null

        assertEquals(3, map.size)
        assertEquals(100, map[k1])
        assertEquals(200, map[k2])
        assertTrue(map.containsKey(null))
        assertTrue(map.containsValue(null))
        assertNull(map[null])

        assertEquals(100, map.remove(k1))
        assertFalse(map.containsKey(k1))
        assertTrue(map.containsKey(k2))
    }

    @Test
    fun testWeakHashMap() {
        val map = WeakHashMap<String, Int>()
        map["one"] = 1
        map["two"] = 2
        assertEquals(2, map.size)
        assertTrue(map.containsKey("one"))
        assertTrue(map.containsValue(2))
        assertEquals(1, map["one"])
        assertEquals(1, map.remove("one"))
        assertEquals(1, map.size)
    }

    @Test
    fun testEnumMapAndEnumSet() {
        val enumMap = EnumMap<Color, String>(Color::class.java)
        enumMap[Color.BLUE] = "Blue"
        enumMap[Color.RED] = "Red"
        enumMap[Color.GREEN] = "Green"

        assertEquals(3, enumMap.size)
        assertEquals(listOf(Color.RED, Color.GREEN, Color.BLUE), enumMap.keys.toList())
        assertEquals("Red", enumMap[Color.RED])

        val clonedMap = enumMap.clone()
        assertEquals(enumMap, clonedMap)
        clonedMap[Color.YELLOW] = "Yellow"
        assertEquals(3, enumMap.size)
        assertEquals(4, clonedMap.size)

        assertFailsWith<IllegalArgumentException> {
            EnumMap(emptyMap<Color, String>())
        }

        val set1 = EnumSet.of(Color.BLUE, Color.RED)
        assertEquals(listOf(Color.RED, Color.BLUE), set1.toList())

        val set2 = EnumSet.noneOf(Color::class.java)
        assertTrue(set2.isEmpty())
        set2.add(Color.YELLOW)
        set2.add(Color.GREEN)
        assertEquals(listOf(Color.GREEN, Color.YELLOW), set2.toList())

        val set3 = EnumSet.copyOf(set1)
        assertEquals(set1, set3)
    }

    @Test
    fun testThreadLocalRandom() {
        val rng = ThreadLocalRandom.current()
        assertSame(rng, ThreadLocalRandom.current())
        assertFailsWith<UnsupportedOperationException> {
            rng.setSeed(42L)
        }
        for (i in 0 until 20) {
            val v = rng.nextInt(10, 20)
            assertTrue(v in 10 until 20)
            val l = rng.nextLong(100L, 200L)
            assertTrue(l in 100L until 200L)
            val d = rng.nextDouble(1.5, 2.5)
            assertTrue(d >= 1.5 && d < 2.5)
        }
    }

    @Test
    fun testArrayBlockingQueueAndPriorityBlockingQueue() {
        val abq = ArrayBlockingQueue<String>(2)
        assertEquals(2, abq.remainingCapacity())
        assertTrue(abq.add("a"))
        assertTrue(abq.offer("b"))
        assertEquals(0, abq.remainingCapacity())
        assertFalse(abq.offer("c"))
        assertFailsWith<IllegalStateException> {
            abq.add("c")
        }

        val drained = mutableListOf<String>()
        assertEquals(2, abq.drainTo(drained))
        assertEquals(listOf("a", "b"), drained)
        assertEquals(2, abq.remainingCapacity())

        val pbq = PriorityBlockingQueue<Int>()
        pbq.put(30)
        pbq.put(10)
        pbq.put(20)
        assertEquals(10, pbq.take())
        assertEquals(20, pbq.poll(50, TimeUnit.MILLISECONDS))
        assertEquals(30, pbq.poll())
        assertNull(pbq.poll())
    }

    @Test
    fun testScheduledExecutorService() {
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        try {
            val future = scheduler.schedule(Callable { 42 }, 20, TimeUnit.MILLISECONDS)
            assertEquals(42, future.get(2, TimeUnit.SECONDS))
            assertTrue(future.isDone)

            val counter = AtomicInteger(0)
            val latch = CountDownLatch(2)
            val periodic = scheduler.scheduleAtFixedRate(
                Runnable {
                    if (counter.incrementAndGet() <= 2) {
                        latch.countDown()
                    }
                },
                5,
                15,
                TimeUnit.MILLISECONDS
            )
            assertTrue(latch.await(2, TimeUnit.SECONDS))
            periodic.cancel(true)
            assertTrue(periodic.isCancelled)
        } finally {
            scheduler.shutdownNow()
        }
    }

    private class TestDelayedItem(val name: String, delayMillis: Long) : Delayed {
        private val triggerNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMillis)

        override fun getDelay(unit: TimeUnit): Long {
            return unit.convert(triggerNanos - System.nanoTime(), TimeUnit.NANOSECONDS)
        }

        override fun compareTo(other: Delayed): Int {
            val d = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS)
            return when {
                d < 0L -> -1
                d > 0L -> 1
                else -> 0
            }
        }
    }

    @Test
    fun testDelayQueue() {
        val dq = DelayQueue<TestDelayedItem>()
        val later = TestDelayedItem("later", 60)
        val sooner = TestDelayedItem("sooner", 10)
        dq.put(later)
        dq.put(sooner)

        assertEquals(2, dq.size)
        assertEquals("sooner", dq.peek()?.name)
        assertEquals("sooner", dq.take().name)
        assertEquals("later", dq.poll(500, TimeUnit.MILLISECONDS)?.name)
        assertNull(dq.poll())
    }

    @Test
    fun testConcurrentSkipListMapAndSet() {
        val map = ConcurrentSkipListMap<Int, String>()
        map[30] = "thirty"
        map[10] = "ten"
        map[20] = "twenty"

        assertEquals(3, map.size)
        assertEquals(10, map.firstKey())
        assertEquals(30, map.lastKey())
        assertEquals(10, map.lowerKey(20))
        assertEquals(20, map.floorKey(20))
        assertEquals(20, map.ceilingKey(15))
        assertEquals(30, map.higherKey(20))
        assertEquals("ten", map.putIfAbsent(10, "ignored"))
        assertTrue(map.replace(20, "twenty", "twenty-updated"))
        assertEquals("twenty-updated", map[20])
        assertEquals(10, map.pollFirstEntry()?.key)
        assertEquals(30, map.pollLastEntry()?.key)
        assertEquals(1, map.size)

        val set = ConcurrentSkipListSet<String>()
        set.addAll(listOf("b", "a", "d", "c"))
        assertEquals(listOf("a", "b", "c", "d"), set.toList())
        val desc = mutableListOf<String>()
        val descIt = set.descendingIterator()
        while (descIt.hasNext()) desc.add(descIt.next())
        assertEquals(listOf("d", "c", "b", "a"), desc)
        assertEquals("a", set.pollFirst())
        assertEquals("d", set.pollLast())
    }

    @Test
    fun testPhaserAndExchanger() {
        val phaser = Phaser(2)
        assertEquals(0, phaser.getPhase())
        assertEquals(2, phaser.getRegisteredParties())

        val worker = Thread(Runnable {
            phaser.arriveAndAwaitAdvance()
            phaser.arriveAndDeregister()
        })
        try {
            worker.start()
        } catch (_: UnsupportedOperationException) {
            phaser.arriveAndDeregister()
            assertEquals(1, phaser.getRegisteredParties())
            phaser.arriveAndDeregister()
            assertTrue(phaser.isTerminated())
            return
        }

        val nextPhase = phaser.arriveAndAwaitAdvance()
        assertEquals(1, nextPhase)
        worker.join()
        assertEquals(1, phaser.getRegisteredParties())
        phaser.arriveAndDeregister()
        assertTrue(phaser.isTerminated())

        val exchanger = Exchanger<String>()
        var receivedByWorker = ""
        val exThread = Thread(Runnable {
            receivedByWorker = exchanger.exchange("from-worker", 2, TimeUnit.SECONDS)
        })
        exThread.start()
        val receivedByMain = exchanger.exchange("from-main", 2, TimeUnit.SECONDS)
        exThread.join()
        assertEquals("from-worker", receivedByMain)
        assertEquals("from-main", receivedByWorker)
    }

    @Test
    fun testLongAdderAndDoubleAdder() {
        val la = LongAdder()
        la.increment()
        la.add(41L)
        la.decrement()
        assertEquals(41L, la.sum())
        assertEquals(41L, la.toLong())
        assertEquals(41L, la.sumThenReset())
        assertEquals(0L, la.sum())

        val da = DoubleAdder()
        da.add(1.5)
        da.add(2.25)
        assertEquals(3.75, da.sum(), 1e-9)
        assertEquals(3.75, da.sumThenReset(), 1e-9)
        assertEquals(0.0, da.sum(), 1e-9)
    }
}


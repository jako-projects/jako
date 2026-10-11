package jako.test.util.concurrent

import java.util.concurrent.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConcurrentCollectionsTest {

    @Test
    fun testConcurrentHashMapAsMutableMap() {
        val map = ConcurrentHashMap<String, Int>()
        assertNull(map.put("a", 1))
        assertEquals(1, map["a"])
        assertEquals(1, map.size)
        assertTrue(map.containsKey("a"))
        assertEquals(1, map.remove("a"))
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
    fun testExecutorsAndThreadPool() {
        val executor = Executors.newSingleThreadExecutor()
        assertFalse(executor.isShutdown())
        executor.shutdown()
        assertTrue(executor.isShutdown())
    }
}

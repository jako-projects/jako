package jako.test.util

import java.util.ArrayDeque
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArrayDequeTest {

    @Test
    fun testDequeAsQueue() {
        val deque = ArrayDeque<String>()
        assertTrue(deque.isEmpty())
        deque.add("A")
        deque.offer("B")
        deque.addLast("C")

        assertEquals(3, deque.size)
        assertEquals("A", deque.peek())
        assertEquals("A", deque.poll())
        assertEquals("B", deque.remove())
        assertEquals("C", deque.poll())
        assertTrue(deque.isEmpty())
    }

    @Test
    fun testDequeAsStack() {
        val stack = ArrayDeque<Int>()
        stack.push(1)
        stack.push(2)
        stack.push(3)

        assertEquals(3, stack.pop())
        assertEquals(2, stack.pop())
        assertEquals(1, stack.pop())
        assertTrue(stack.isEmpty())
    }

    @Test
    fun testDescendingIterator() {
        val deque = ArrayDeque<Int>()
        deque.add(10)
        deque.add(20)
        deque.add(30)

        val list = mutableListOf<Int>()
        val iter = deque.descendingIterator()
        while (iter.hasNext()) {
            list.add(iter.next())
        }
        assertEquals(listOf(30, 20, 10), list)
    }
}

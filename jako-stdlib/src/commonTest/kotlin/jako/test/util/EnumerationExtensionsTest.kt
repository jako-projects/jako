package jako.test.util

import java.util.Enumeration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnumerationExtensionsTest {

    private fun <T> createEnum(elements: List<T>): Enumeration<T> {
        var idx = 0
        return object : Enumeration<T> {
            override fun hasMoreElements(): Boolean = idx < elements.size
            override fun nextElement(): T = elements[idx++]
        }
    }

    @Test
    fun testEnumerationIteratorAndToList() {
        val data = listOf("One", "Two", "Three")
        val e1 = createEnum(data)

        val list = e1.toList()
        assertEquals(3, list.size)
        assertEquals("One", list[0])
        assertEquals("Two", list[1])
        assertEquals("Three", list[2])

        val e2 = createEnum(data)
        val iterator = e2.iterator()
        assertTrue(iterator.hasNext())
        assertEquals("One", iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals("Two", iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals("Three", iterator.next())
    }
}

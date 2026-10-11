package jako.test.util

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Base64AndUtilTest {

    @Test
    fun testBase64Encoding() {
        val original = "Hello Jako Multiplatform!"
        val encoded = Base64.getEncoder().encodeToString(original.encodeToByteArray())
        val decoded = Base64.getDecoder().decode(encoded).decodeToString()
        assertEquals(original, decoded)
    }

    @Test
    fun testRandomGeneration() {
        val rand = Random(42L)
        val val1 = rand.nextInt(100)
        assertTrue(val1 in 0 until 100)
        val boolVal = rand.nextBoolean()
        assertTrue(boolVal || !boolVal)
    }

    @Test
    fun testObjectsUtils() {
        assertTrue(Objects.equals("abc", "abc"))
        assertFalse(Objects.equals("abc", "def"))
        assertEquals("test", Objects.requireNonNull("test"))
        assertTrue(Objects.isNull(null))
        assertTrue(Objects.nonNull("non-null"))
    }

    @Test
    fun testArrayDeque() {
        val deque = ArrayDeque<String>()
        deque.addFirst("first")
        deque.addLast("last")
        assertEquals("first", deque.peekFirst())
        assertEquals("last", deque.peekLast())
        assertEquals("first", deque.removeFirst())
        assertEquals("last", deque.removeLast())
    }
}

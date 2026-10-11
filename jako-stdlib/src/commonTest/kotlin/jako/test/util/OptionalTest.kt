package jako.test.util

import java.util.NoSuchElementException
import java.util.Optional
import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.OptionalLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OptionalTest {

    @Test
    fun testOptionalBasic() {
        val opt = Optional.of("hello")
        assertTrue(opt.isPresent())
        assertEquals("hello", opt.get())
        assertEquals("hello", opt.orElse("world"))

        val empty = Optional.empty<String>()
        assertFalse(empty.isPresent())
        assertEquals("world", empty.orElse("world"))
        assertEquals("computed", empty.orElseGet { "computed" })

        assertFailsWith<NoSuchElementException> {
            empty.get()
        }
    }

    @Test
    fun testOptionalMapAndFilter() {
        val opt = Optional.of("123")
        val mapped = opt.map { it.toInt() }
        assertTrue(mapped.isPresent())
        assertEquals(123, mapped.get())

        val filtered = opt.filter { it.startsWith("1") }
        assertTrue(filtered.isPresent())

        val notFiltered = opt.filter { it.startsWith("9") }
        assertFalse(notFiltered.isPresent())
    }

    @Test
    fun testPrimitiveOptionals() {
        val optInt = OptionalInt.of(42)
        assertTrue(optInt.isPresent())
        assertEquals(42, optInt.getAsInt())

        val emptyInt = OptionalInt.empty()
        assertFalse(emptyInt.isPresent())
        assertEquals(10, emptyInt.orElse(10))

        val optLong = OptionalLong.of(100L)
        assertTrue(optLong.isPresent())
        assertEquals(100L, optLong.getAsLong())

        val optDouble = OptionalDouble.of(3.14)
        assertTrue(optDouble.isPresent())
        assertEquals(3.14, optDouble.getAsDouble())
    }
}

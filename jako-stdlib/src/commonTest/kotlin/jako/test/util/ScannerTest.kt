package jako.test.util

import java.util.Scanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScannerTest {

    @Test
    fun testTokenScanning() {
        val scanner = Scanner("123 456.78 true hello world")
        assertTrue(scanner.hasNextInt())
        assertEquals(123, scanner.nextInt())

        assertTrue(scanner.hasNextDouble())
        assertEquals(456.78, scanner.nextDouble())

        assertTrue(scanner.hasNextBoolean())
        assertEquals(true, scanner.nextBoolean())

        assertTrue(scanner.hasNext())
        assertEquals("hello", scanner.next())

        assertTrue(scanner.hasNext())
        assertEquals("world", scanner.next())

        assertFalse(scanner.hasNext())
        scanner.close()
    }

    @Test
    fun testLineScanning() {
        val scanner = Scanner("Line 1\nLine 2\r\nLine 3")
        assertTrue(scanner.hasNextLine())
        assertEquals("Line 1", scanner.nextLine())

        assertTrue(scanner.hasNextLine())
        assertEquals("Line 2", scanner.nextLine())

        assertTrue(scanner.hasNextLine())
        assertEquals("Line 3", scanner.nextLine())

        assertFalse(scanner.hasNextLine())
        scanner.close()
    }

    @Test
    fun testCustomDelimiter() {
        val scanner = Scanner("apple,banana,cherry").useDelimiter(",")
        assertEquals("apple", scanner.next())
        assertEquals("banana", scanner.next())
        assertEquals("cherry", scanner.next())
        assertFalse(scanner.hasNext())
        scanner.close()
    }
}

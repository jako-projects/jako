package jako.test.kotlin

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.Paths
import java.util.Optional
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Pattern
import kotlin.jvm.optionals.asSequence
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import kotlin.jvm.optionals.toList
import kotlin.concurrent.withLock
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KotlinStdlibExtensionsTest {

    @Test
    fun testFileAndIOExtensions() {
        val tmpBase = File(java.lang.System.getProperty("java.io.tmpdir") ?: "/tmp")
        val temp = File(tmpBase, "jako_kt_ext_${java.lang.System.currentTimeMillis()}.txt")
        try {
            // Note: kotlin.io is auto-imported in Kotlin, so writeText and readText are available without import
            temp.writeText("Kotlin stdlib IO extension test")
            assertEquals("Kotlin stdlib IO extension test", temp.readText())

            temp.appendText("\nSecond Line")
            val lines = temp.readLines()
            assertEquals(2, lines.size)
            assertEquals("Second Line", lines[1])

            assertEquals("txt", temp.extension)
            assertTrue(temp.nameWithoutExtension.startsWith("jako_kt_ext_"))

            val bytes = temp.readBytes()
            assertTrue(bytes.isNotEmpty())
        } finally {
            temp.delete()
        }
    }

    @Test
    fun testStreamExtensions() {
        val inStream = ByteArrayInputStream("Hello Stream".encodeToByteArray())
        val readBytes = inStream.readBytes()
        assertEquals("Hello Stream", readBytes.decodeToString())

        val inStream2 = ByteArrayInputStream("Copy Test".encodeToByteArray())
        val outStream = ByteArrayOutputStream()
        val copied = inStream2.copyTo(outStream)
        assertEquals(9L, copied)
        assertEquals("Copy Test", outStream.toByteArray().decodeToString())

        val reader = StringReader("Line 1\nLine 2")
        val readerLines = reader.readLines()
        assertEquals(2, readerLines.size)
        assertEquals("Line 1", readerLines[0])
    }

    @Test
    fun testPathDivAndExtensions() {
        val base = Paths.get("/tmp")
        val child = base / "child_path.log"
        assertEquals("/tmp/child_path.log", child.toString().replace('\\', '/'))
        assertEquals("log", child.extension)
        assertEquals("child_path", child.nameWithoutExtension)
    }

    @Test
    fun testLockWithLock() {
        val lock = ReentrantLock()
        var executed = false
        val res = lock.withLock {
            executed = true
            "locked_result"
        }
        assertTrue(executed)
        assertEquals("locked_result", res)
    }

    @Test
    fun testOptionalExtensions() {
        val opt = Optional.of("Antigravity")
        assertEquals("Antigravity", opt.getOrNull())
        assertEquals("Antigravity", opt.getOrDefault("Fallback"))
        assertEquals("Antigravity", opt.getOrElse { "Computed" })
        assertEquals(listOf("Antigravity"), opt.toList())
        assertEquals(listOf("Antigravity"), opt.asSequence().toList())

        val empty = Optional.empty<String>()
        assertNull(empty.getOrNull())
        assertEquals("Fallback", empty.getOrDefault("Fallback"))
        assertEquals("Computed", empty.getOrElse { "Computed" })
        assertTrue(empty.toList().isEmpty())
        assertTrue(empty.asSequence().toList().isEmpty())
    }

    @Test
    fun testRegexAndPatternExtensions() {
        val pattern = Pattern.compile("abc\\d+")
        val regex = pattern.toRegex()
        assertTrue(regex.matches("abc123"))

        val pattern2 = Regex("xyz\\w+").toPattern()
        assertTrue(pattern2.matcher("xyz_test").matches())
    }
}

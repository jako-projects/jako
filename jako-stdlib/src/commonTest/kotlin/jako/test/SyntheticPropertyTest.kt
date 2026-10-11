package jako.test

import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Thread
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URL
import java.nio.ByteBuffer
import java.security.cert.Certificate
import java.text.ParsePosition
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SyntheticPropertyTest {

    @Test
    fun testFileSyntheticProperties() {
        val file = File("/tmp/test_synthetic.txt")
        assertEquals("/tmp/test_synthetic.txt", file.path.replace('\\', '/'))
        assertEquals("test_synthetic.txt", file.name)
        assertEquals("/tmp", file.parent?.replace('\\', '/'))
        assertNotNull(file.parentFile)
        assertNotNull(file.absolutePath)
        assertNotNull(file.absoluteFile)
        assertTrue(file.absoluteFile.isAbsolute)
        assertFalse(file.isDirectory)
        assertFalse(file.isFile)
    }

    @Test
    fun testByteArrayOutputStreamSyntheticProperties() {
        val baos = ByteArrayOutputStream(16)
        baos.write(65)
        assertEquals(1, baos.size())
    }

    @Test
    fun testThreadSyntheticProperties() {
        val t = Thread("CustomThread")
        assertEquals("CustomThread", t.name)
        assertFalse(t.isInterrupted)
    }

    @Test
    fun testNetSyntheticProperties() {
        val addr = InetAddress.getByAddress("localhost", byteArrayOf(127, 0, 0, 1))
        assertEquals("localhost", addr.hostName)
        assertEquals("127.0.0.1", addr.hostAddress)
        assertNotNull(addr.address)

        val socketAddr = InetSocketAddress("127.0.0.1", 8080)
        assertEquals(8080, socketAddr.port)
        assertNotNull(socketAddr.address)

        val url = URL("https://example.com:8080/test/path")
        assertEquals("https", url.protocol)
        assertEquals("example.com", url.host)
        assertEquals(8080, url.port)
        assertEquals("/test/path", url.path)
    }

    @Test
    fun testNioSyntheticProperties() {
        val buf = ByteBuffer.allocate(10)
        assertFalse(buf.isReadOnly)
        assertFalse(buf.isDirect)
    }

    @Test
    fun testTextSyntheticProperties() {
        val pos = ParsePosition(5)
        assertEquals(5, pos.index)
        assertEquals(-1, pos.errorIndex)
    }

    @Test
    fun testUtilSyntheticProperties() {
        val date = Date(1000L)
        assertEquals(1000L, date.time)

        val tz = TimeZone.getTimeZone("UTC")
        assertEquals("UTC", tz.id)
        assertEquals(0, tz.rawOffset)

        val loc = Locale("en", "US")
        assertEquals("en", loc.language)
        assertEquals("US", loc.country)

        val cal = Calendar.getInstance()
        assertNotNull(cal.timeZone)
        assertNotNull(cal.time)

        val uuid = UUID(12345L, 67890L)
        assertEquals(12345L, uuid.mostSignificantBits)
        assertEquals(67890L, uuid.leastSignificantBits)
    }

    @Test
    fun testLocksSyntheticProperties() {
        val lock = ReentrantLock()
        assertFalse(lock.isLocked)
        assertFalse(lock.isHeldByCurrentThread)
        assertEquals(0, lock.holdCount)
        assertFalse(lock.isFair)
    }
}

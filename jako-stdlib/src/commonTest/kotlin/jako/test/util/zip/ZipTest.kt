package jako.test.util.zip

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.System
import java.util.zip.Adler32
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ZipTest {

    @Test
    fun testCRC32() {
        val crc = CRC32()
        assertEquals(0L, crc.getValue())

        val data = "123456789".encodeToByteArray()
        crc.update(data)
        // Known CRC32 of "123456789" is 0xCBF43926 = 3421780262L
        assertEquals(3421780262L, crc.getValue())

        crc.reset()
        assertEquals(0L, crc.getValue())
    }

    @Test
    fun testAdler32() {
        val adler = Adler32()
        assertEquals(1L, adler.getValue())

        val data = "Wikipedia".encodeToByteArray()
        adler.update(data)
        // Known Adler32 of "Wikipedia" is 0x11E60398 = 300286872L
        assertEquals(300286872L, adler.getValue())

        adler.reset()
        assertEquals(1L, adler.getValue())
    }

    @Test
    fun testZipEntry() {
        val entry = ZipEntry("test/dir/file.txt")
        assertEquals("test/dir/file.txt", entry.name)
        assertFalse(entry.isDirectory)

        entry.size = 1024L
        assertEquals(1024L, entry.size)

        entry.time = 123456789L
        assertEquals(123456789L, entry.time)

        entry.comment = "Test comment"
        assertEquals("Test comment", entry.comment)

        val dirEntry = ZipEntry("test/dir/")
        assertTrue(dirEntry.isDirectory)
    }

    @Test
    fun testDeflaterInflaterRoundtrip() {
        val input = "The quick brown fox jumps over the lazy dog. ".repeat(40).encodeToByteArray()
        val def = Deflater()
        try {
            def.setInput(input)
            def.finish()
            val compressed = ByteArrayOutputStream()
            val buf = ByteArray(64)
            while (!def.finished()) {
                val n = def.deflate(buf)
                if (n > 0) compressed.write(buf, 0, n)
            }
            assertTrue(def.getBytesRead() >= input.size.toLong())
            assertTrue(def.getBytesWritten() > 0L)

            val inf = Inflater()
            try {
                val deflated = compressed.toByteArray()
                inf.setInput(deflated)
                val out = ByteArrayOutputStream()
                while (!inf.finished()) {
                    val n = inf.inflate(buf)
                    if (n > 0) out.write(buf, 0, n)
                    if (n == 0 && inf.needsInput()) break
                }
                assertContentEquals(input, out.toByteArray())
                assertEquals(input.size.toLong(), inf.getBytesWritten())
            } finally {
                inf.end()
            }
        } finally {
            def.end()
        }
    }

    @Test
    fun testDeflaterInflaterEmpty() {
        val def = Deflater()
        try {
            def.setInput(ByteArray(0))
            def.finish()
            val compressed = ByteArrayOutputStream()
            val buf = ByteArray(32)
            while (!def.finished()) {
                val n = def.deflate(buf)
                if (n > 0) compressed.write(buf, 0, n)
            }
            val inf = Inflater()
            try {
                inf.setInput(compressed.toByteArray())
                val n = inf.inflate(buf)
                assertEquals(0, n)
                assertTrue(inf.finished())
            } finally {
                inf.end()
            }
        } finally {
            def.end()
        }
    }

    @Test
    fun testGZIPRoundtripUtf8() {
        val text = "Hello, GZIP 压缩测试 🎉\nsecond line".encodeToByteArray()
        val baos = ByteArrayOutputStream()
        val gos = GZIPOutputStream(baos)
        try {
            gos.write(text)
            gos.finish()
        } finally {
            gos.close()
        }
        val compressed = baos.toByteArray()
        assertTrue(compressed.size >= 18)
        assertEquals(0x1f, compressed[0].toInt() and 0xFF)
        assertEquals(0x8b, compressed[1].toInt() and 0xFF)

        val gis = GZIPInputStream(ByteArrayInputStream(compressed))
        try {
            val out = ByteArrayOutputStream()
            val buf = ByteArray(32)
            var n = gis.read(buf)
            while (n >= 0) {
                out.write(buf, 0, n)
                n = gis.read(buf)
            }
            assertContentEquals(text, out.toByteArray())
        } finally {
            gis.close()
        }
    }

    @Test
    fun testGZIPKnownJdkBytes() {
        // JDK 8 GZIPOutputStream of UTF-8 "hello" (MTIME=0, OS=0).
        val jdkGzip = byteArrayOf(
            0x1f, 0x8b.toByte(), 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0xcb.toByte(), 0x48, 0xcd.toByte(), 0xc9.toByte(), 0xc9.toByte(), 0x07, 0x00,
            0x86.toByte(), 0xa6.toByte(), 0x10, 0x36, 0x05, 0x00, 0x00, 0x00
        )
        val gis = GZIPInputStream(ByteArrayInputStream(jdkGzip))
        try {
            val out = ByteArrayOutputStream()
            val buf = ByteArray(16)
            var n = gis.read(buf)
            while (n >= 0) {
                out.write(buf, 0, n)
                n = gis.read(buf)
            }
            assertContentEquals("hello".encodeToByteArray(), out.toByteArray())
        } finally {
            gis.close()
        }
    }

    @Test
    fun testGZIPCorruptMagicThrowsZipException() {
        assertFailsWith<ZipException> {
            GZIPInputStream(ByteArrayInputStream(byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)))
        }
    }

    @Test
    fun testGZIPCorruptBodyThrows() {
        val header = byteArrayOf(0x1f, 0x8b.toByte(), 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val corrupt = header + byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val gis = GZIPInputStream(ByteArrayInputStream(corrupt))
        try {
            assertFailsWith<IOException> {
                val buf = ByteArray(32)
                while (gis.read(buf) != -1) {
                }
            }
        } finally {
            try {
                gis.close()
            } catch (_: Exception) {
            }
        }
    }

    @Test
    fun testZipOutputInputAndZipFileRoundtrip() {
        val hello = "hello zip".encodeToByteArray()
        val second = "second file content 第二".encodeToByteArray()
        val storedPayload = "stored-plain".encodeToByteArray()

        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)
        try {
            zos.putNextEntry(ZipEntry("hello.txt"))
            zos.write(hello)
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("dir/second.txt"))
            zos.write(second)
            zos.closeEntry()

            val stored = ZipEntry("stored.txt")
            stored.method = ZipEntry.STORED
            stored.size = storedPayload.size.toLong()
            stored.compressedSize = storedPayload.size.toLong()
            val crc = CRC32()
            crc.update(storedPayload)
            stored.crc = crc.getValue()
            zos.putNextEntry(stored)
            zos.write(storedPayload)
            zos.closeEntry()

            zos.finish()
        } finally {
            zos.close()
        }

        val zipBytes = baos.toByteArray()
        assertTrue(zipBytes.size > 4)
        assertEquals(0x50, zipBytes[0].toInt() and 0xFF)
        assertEquals(0x4b, zipBytes[1].toInt() and 0xFF)

        val zis = ZipInputStream(ByteArrayInputStream(zipBytes))
        try {
            val e1 = zis.getNextEntry()
            assertNotNull(e1)
            assertEquals("hello.txt", e1!!.name)
            assertContentEquals(hello, readAll(zis))
            zis.closeEntry()

            val e2 = zis.getNextEntry()
            assertNotNull(e2)
            assertEquals("dir/second.txt", e2!!.name)
            assertContentEquals(second, readAll(zis))

            val e3 = zis.getNextEntry()
            assertNotNull(e3)
            assertEquals("stored.txt", e3!!.name)
            assertEquals(ZipEntry.STORED, e3.method)
            assertContentEquals(storedPayload, readAll(zis))

            assertNull(zis.getNextEntry())
        } finally {
            zis.close()
        }

        val tmpBase = File(System.getProperty("java.io.tmpdir") ?: "/tmp")
        val tmp = File(tmpBase, "jako_zip_test_" + System.currentTimeMillis() + ".zip")
        try {
            tmp.writeBytes(zipBytes)
            val zf = ZipFile(tmp)
            try {
                assertEquals(3, zf.size())
                val names = mutableListOf<String>()
                val en = zf.entries()
                while (en.hasMoreElements()) {
                    names.add(en.nextElement().name)
                }
                assertTrue(names.contains("hello.txt"))
                assertTrue(names.contains("dir/second.txt"))
                assertTrue(names.contains("stored.txt"))

                val entry = zf.getEntry("hello.txt")
                assertNotNull(entry)
                val entryIn = zf.getInputStream(entry!!)
                assertNotNull(entryIn)
                try {
                    assertContentEquals(hello, readAll(entryIn!!))
                } finally {
                    entryIn.close()
                }

                val storedEntry = zf.getEntry("stored.txt")
                assertNotNull(storedEntry)
                val storedIn = zf.getInputStream(storedEntry!!)
                assertNotNull(storedIn)
                try {
                    assertContentEquals(storedPayload, readAll(storedIn!!))
                } finally {
                    storedIn.close()
                }

                assertNull(zf.getEntry("missing.txt"))
            } finally {
                zf.close()
            }
        } finally {
            tmp.delete()
        }
    }

    @Test
    fun testDeflaterOutputStreamAndInflaterInputStream() {
        assertFailsWith<IllegalArgumentException> {
            java.util.zip.DeflaterOutputStream(ByteArrayOutputStream(), Deflater(), 0)
        }
        assertFailsWith<IllegalArgumentException> {
            java.util.zip.InflaterInputStream(ByteArrayInputStream(ByteArray(0)), Inflater(), 0)
        }

        val payload = "DeflaterOutputStream & InflaterInputStream roundtrip test! ".repeat(10).encodeToByteArray()
        val baos = ByteArrayOutputStream()
        val dos = java.util.zip.DeflaterOutputStream(baos, true)
        try {
            dos.write(payload[0].toInt() and 0xFF)
            dos.write(payload, 1, payload.size - 1)
            dos.flush()
            dos.finish()
        } finally {
            dos.close()
        }
        assertFailsWith<IOException> {
            dos.write(1)
        }

        val compressed = baos.toByteArray()
        assertTrue(compressed.isNotEmpty())

        val iis = java.util.zip.InflaterInputStream(ByteArrayInputStream(compressed))
        try {
            assertFalse(iis.markSupported())
            assertEquals(1, iis.available())
            assertFailsWith<IOException> { iis.reset() }
            assertFailsWith<IllegalArgumentException> { iis.skip(-1L) }

            val firstByte = iis.read()
            assertEquals(payload[0].toInt() and 0xFF, firstByte)

            val skipped = iis.skip(5L)
            assertEquals(5L, skipped)

            val remaining = readAll(iis)
            assertContentEquals(payload.copyOfRange(6, payload.size), remaining)
            assertEquals(0, iis.available())
            assertEquals(-1, iis.read())
        } finally {
            iis.close()
        }
        assertFailsWith<IOException> {
            iis.read()
        }
    }

    private fun readAll(input: java.io.InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(64)
        var n = input.read(buf)
        while (n >= 0) {
            out.write(buf, 0, n)
            n = input.read(buf)
        }
        return out.toByteArray()
    }
}

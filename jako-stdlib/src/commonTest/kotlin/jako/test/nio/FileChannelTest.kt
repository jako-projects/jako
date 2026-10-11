package jako.test.nio

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileChannelTest {

    @Test
    fun testWriteThenReadBack() {
        val dir = Paths.get("build", "tmp", "filechannel")
        Files.createDirectories(dir)
        val path = dir.resolve("roundtrip.bin")
        Files.deleteIfExists(path)

        val payload = "hello filechannel 你好".encodeToByteArray()
        val writeCh = FileChannel.open(
            path,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        try {
            val written = writeCh.write(ByteBuffer.wrap(payload))
            assertEquals(payload.size, written)
            assertEquals(payload.size.toLong(), writeCh.size())
            assertEquals(payload.size.toLong(), writeCh.position())
        } finally {
            writeCh.close()
        }

        val readCh = FileChannel.open(path, StandardOpenOption.READ)
        try {
            assertEquals(payload.size.toLong(), readCh.size())
            assertEquals(0L, readCh.position())
            val buf = ByteBuffer.allocate(payload.size)
            val n = readCh.read(buf)
            assertEquals(payload.size, n)
            buf.flip()
            val got = ByteArray(buf.remaining())
            buf.get(got)
            assertTrue(got.contentEquals(payload))
            assertEquals(-1, readCh.read(ByteBuffer.allocate(1)))
        } finally {
            readCh.close()
        }

        Files.deleteIfExists(path)
    }

    @Test
    fun testPositionAndSize() {
        val dir = Paths.get("build", "tmp", "filechannel")
        Files.createDirectories(dir)
        val path = dir.resolve("position.bin")
        Files.deleteIfExists(path)

        val ch = FileChannel.open(
            path,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        try {
            ch.write(ByteBuffer.wrap(byteArrayOf(10, 20, 30, 40)))
            assertEquals(4L, ch.size())
            ch.position(1L)
            val buf = ByteBuffer.allocate(2)
            assertEquals(2, ch.read(buf))
            buf.flip()
            assertEquals(20, buf.get().toInt())
            assertEquals(30, buf.get().toInt())
            assertEquals(3L, ch.position())
        } finally {
            ch.close()
        }

        Files.deleteIfExists(path)
    }
}

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

    @Test
    fun testTruncateForceAndChannels() {
        val dir = Paths.get("build", "tmp", "filechannel")
        Files.createDirectories(dir)
        val path = dir.resolve("channels_test.txt")
        Files.deleteIfExists(path)

        val ch = FileChannel.open(
            path,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        try {
            val writer = java.nio.channels.Channels.newWriter(ch, "UTF-8")
            writer.write("0123456789ABCDEF")
            writer.flush()
            ch.force(true)
            assertEquals(16L, ch.size())

            ch.truncate(10L)
            assertEquals(10L, ch.size())
            assertEquals(10L, ch.position())

            ch.position(0L)
            val reader = java.nio.channels.Channels.newReader(ch, "UTF-8")
            val chars = CharArray(10)
            val n = reader.read(chars, 0, 10)
            assertEquals(10, n)
            assertEquals("0123456789", chars.concatToString())
        } finally {
            ch.close()
        }

        val baos = java.io.ByteArrayOutputStream()
        val writableCh = java.nio.channels.Channels.newChannel(baos)
        writableCh.write(ByteBuffer.wrap("channel-stream".encodeToByteArray()))
        writableCh.close()

        val bais = java.io.ByteArrayInputStream(baos.toByteArray())
        val readableCh = java.nio.channels.Channels.newChannel(bais)
        val streamIn = java.nio.channels.Channels.newInputStream(readableCh)
        val readBuf = ByteArray(14)
        assertEquals(14, streamIn.read(readBuf))
        assertEquals("channel-stream", readBuf.decodeToString())
        streamIn.close()

        Files.deleteIfExists(path)
    }

    @Test
    fun testPositionalReadWriteAndScatterGather() {
        val dir = Paths.get("build", "tmp", "filechannel")
        Files.createDirectories(dir)
        val path = dir.resolve("positional_scatter.bin")
        Files.deleteIfExists(path)

        val ch = FileChannel.open(
            path,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        try {
            val b1 = ByteBuffer.wrap("Hello ".encodeToByteArray())
            val b2 = ByteBuffer.wrap("World!".encodeToByteArray())
            val written = ch.write(arrayOf(b1, b2))
            assertEquals(12L, written)
            assertEquals(12L, ch.position())

            // Positional write does not alter channel position
            ch.position(2L)
            val patch = ByteBuffer.wrap("KOTLIN".encodeToByteArray())
            assertEquals(6, ch.write(patch, 6L))
            assertEquals(2L, ch.position())

            // Positional read does not alter channel position
            val posReadBuf = ByteBuffer.allocate(6)
            assertEquals(6, ch.read(posReadBuf, 6L))
            assertEquals(2L, ch.position())
            posReadBuf.flip()
            val posBytes = ByteArray(6)
            posReadBuf.get(posBytes)
            assertEquals("KOTLIN", posBytes.decodeToString())

            // Scatter read from beginning
            ch.position(0L)
            val r1 = ByteBuffer.allocate(6)
            val r2 = ByteBuffer.allocate(6)
            assertEquals(12L, ch.read(arrayOf(r1, r2)))
            r1.flip()
            r2.flip()
            val s1 = ByteArray(6).also { r1.get(it) }.decodeToString()
            val s2 = ByteArray(6).also { r2.get(it) }.decodeToString()
            assertEquals("Hello ", s1)
            assertEquals("KOTLIN", s2)
        } finally {
            ch.close()
            Files.deleteIfExists(path)
        }
    }

    @Test
    fun testTransferToAndTransferFrom() {
        val dir = Paths.get("build", "tmp", "filechannel")
        Files.createDirectories(dir)
        val srcPath = dir.resolve("transfer_src.bin")
        val dstPath = dir.resolve("transfer_dst.bin")
        val dst2Path = dir.resolve("transfer_dst2.bin")
        Files.deleteIfExists(srcPath)
        Files.deleteIfExists(dstPath)
        Files.deleteIfExists(dst2Path)

        val srcCh = FileChannel.open(
            srcPath,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        val dstCh = FileChannel.open(
            dstPath,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        val dst2Ch = FileChannel.open(
            dst2Path,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        try {
            val content = "0123456789abcdefghijklmnopqrstuvwxyz".encodeToByteArray()
            srcCh.write(ByteBuffer.wrap(content))
            srcCh.position(0L)

            val transferred = srcCh.transferTo(10L, 16L, dstCh)
            assertEquals(16L, transferred)
            assertEquals(0L, srcCh.position()) // transferTo does not modify srcCh position
            assertEquals(16L, dstCh.size())

            dstCh.position(0L)
            val transferredFrom = dst2Ch.transferFrom(dstCh, 0L, 16L)
            assertEquals(16L, transferredFrom)
            val verifyBuf = ByteBuffer.allocate(16)
            assertEquals(16, dst2Ch.read(verifyBuf, 0L))
            verifyBuf.flip()
            val verifyBytes = ByteArray(16).also { verifyBuf.get(it) }
            assertEquals("abcdefghijklmnop", verifyBytes.decodeToString())
        } finally {
            srcCh.close()
            dstCh.close()
            dst2Ch.close()
            Files.deleteIfExists(srcPath)
            Files.deleteIfExists(dstPath)
            Files.deleteIfExists(dst2Path)
        }
    }

    @Test
    fun testFileLockAndRandomAccessFileChannel() {
        val dir = Paths.get("build", "tmp", "filechannel")
        Files.createDirectories(dir)
        val path = dir.resolve("lock_and_raf.bin")
        Files.deleteIfExists(path)

        val raf = java.io.RandomAccessFile(path.toFile(), "rw")
        try {
            assertTrue(raf.fd.valid())
            raf.writeBytes("ABCDEF")
            val ch = raf.channel
            assertEquals(6L, ch.position())
            assertEquals(6L, ch.size())

            ch.position(2L)
            assertEquals(2L, raf.filePointer)

            val lock1: java.nio.channels.FileLock = ch.lock(0L, 10L, false)
            assertEquals(ch, lock1.channel())
            assertEquals(ch, lock1.acquiredBy())
            assertEquals(0L, lock1.position())
            assertEquals(10L, lock1.size())
            assertTrue(lock1.isValid)
            assertTrue(lock1.isValid())
            assertTrue(!lock1.isShared)
            assertTrue(!lock1.isShared())
            assertTrue(lock1.overlaps(5L, 10L))
            assertTrue(!lock1.overlaps(10L, 5L))
            assertTrue(!lock1.overlaps(0L, 0L))

            var overlappingThrown = false
            try {
                ch.tryLock(5L, 10L, false)
            } catch (_: java.nio.channels.OverlappingFileLockException) {
                overlappingThrown = true
            }
            assertTrue(overlappingThrown)

            // Non-overlapping lock on [10, 20) succeeds
            val lock2: java.nio.channels.FileLock = ch.tryLock(10L, 10L, false)!!
            assertEquals(10L, lock2.position())
            assertEquals(10L, lock2.size())
            assertTrue(lock2.isValid)
            lock2.close()
            assertTrue(!lock2.isValid)

            lock1.release()
            assertTrue(!lock1.isValid)
        } finally {
            raf.close()
            Files.deleteIfExists(path)
        }
    }
}



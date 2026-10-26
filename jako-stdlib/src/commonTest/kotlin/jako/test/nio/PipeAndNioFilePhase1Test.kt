package jako.test.nio

import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.Pipe
import java.nio.file.*
import kotlin.test.*

class PipeAndNioFilePhase1Test {

    @Test
    fun testPipeReadWrite() {
        val pipe = Pipe.open()
        val sink: Pipe.SinkChannel = pipe.sink()
        val source: Pipe.SourceChannel = pipe.source()
        try {
            assertEquals(java.nio.channels.SelectionKey.OP_WRITE, sink.validOps())
            assertEquals(java.nio.channels.SelectionKey.OP_READ, source.validOps())
            assertTrue(sink.isOpen())
            assertTrue(source.isOpen())

            val message = "Hello from Pipe!".encodeToByteArray()
            val writeBuf = ByteBuffer.wrap(message)
            val written = sink.write(writeBuf)
            assertEquals(message.size, written)

            val readBuf = ByteBuffer.allocate(64)
            val read = source.read(readBuf)
            assertEquals(message.size, read)
            readBuf.flip()
            val received = ByteArray(read)
            readBuf.get(received)
            assertEquals("Hello from Pipe!", received.decodeToString())

            // Gathering write & scattering read
            val g1 = ByteBuffer.wrap("Part1-".encodeToByteArray())
            val g2 = ByteBuffer.wrap("Part2".encodeToByteArray())
            assertEquals(11L, sink.write(arrayOf(g1, g2)))

            val s1 = ByteBuffer.allocate(6)
            val s2 = ByteBuffer.allocate(5)
            assertEquals(11L, source.read(arrayOf(s1, s2)))
            s1.flip()
            s2.flip()
            assertEquals("Part1-", ByteArray(6).also { s1.get(it) }.decodeToString())
            assertEquals("Part2", ByteArray(5).also { s2.get(it) }.decodeToString())

            // Read-only buffer throws IllegalArgumentException per ReadableByteChannel contract
            assertFailsWith<IllegalArgumentException> {
                source.read(ByteBuffer.allocate(4).asReadOnlyBuffer())
            }

            // Non-blocking read when empty returns 0, and -1 after sink closes
            source.configureBlocking(false)
            assertFalse(source.isBlocking())
            assertEquals(0, source.read(ByteBuffer.allocate(4)))
            sink.close()
            assertFalse(sink.isOpen())
            assertEquals(-1, source.read(ByteBuffer.allocate(4)))
        } finally {
            sink.close()
            source.close()
        }
        assertFalse(source.isOpen())
        assertFailsWith<java.nio.channels.ClosedChannelException> {
            source.read(ByteBuffer.allocate(4))
        }
        assertFailsWith<java.nio.channels.ClosedChannelException> {
            sink.write(ByteBuffer.wrap(byteArrayOf(1)))
        }

        val ex1 = AsynchronousCloseException()
        assertNull(ex1.message)
        val ex2 = ClosedByInterruptException()
        assertNull(ex2.message)
    }

    @Test
    fun testFileUriPathAndPermissions() {
        val tempDir = Files.createTempDirectory("jako_file_phase1_")
        val tempFile = Files.createTempFile(tempDir, "sample_", ".txt")
        try {
            val file = tempFile.toFile()
            assertTrue(file.exists())
            assertEquals(tempFile.toAbsolutePath().normalize(), file.toPath().toAbsolutePath().normalize())

            val uri = file.toURI()
            assertEquals("file", uri.getScheme())
            val fromUri = File(uri)
            assertEquals(file.getCanonicalPath(), fromUri.getCanonicalPath())

            file.deleteOnExit()
            assertTrue(file.setWritable(false))
            assertTrue(file.setWritable(true))
            assertTrue(file.setReadable(true))
        } finally {
            Files.deleteIfExists(tempFile)
            Files.deleteIfExists(tempDir)
        }
    }

    @Test
    fun testDirectoryStreamPathMatcherAndSameFile() {
        val dir = Files.createTempDirectory("jako_dirstream_")
        val f1 = Files.createFile(dir.resolve("alpha.kt"))
        val f2 = Files.createFile(dir.resolve("beta.kt"))
        val f3 = Files.createFile(dir.resolve("notes.md"))
        val hidden = Files.createFile(dir.resolve(".hidden"))
        try {
            assertTrue(Files.isSameFile(f1, dir.resolve("alpha.kt")))
            assertFalse(Files.isSameFile(f1, f2))
            assertFalse(Files.isHidden(f1))
            val isWindows = System.getProperty("os.name")?.lowercase()?.contains("win") == true
            if (!isWindows) {
                assertTrue(Files.isHidden(hidden))
            }

            val allNames = mutableSetOf<String>()
            val s1 = Files.newDirectoryStream(dir)
            try {
                for (p in s1) {
                    allNames.add(p.getFileName()!!.toString())
                }
            } finally {
                s1.close()
            }
            assertTrue(allNames.containsAll(listOf("alpha.kt", "beta.kt", "notes.md", ".hidden")))

            val ktNames = mutableSetOf<String>()
            val s2 = Files.newDirectoryStream(dir, "*.kt")
            try {
                for (p in s2) {
                    ktNames.add(p.getFileName()!!.toString())
                }
            } finally {
                s2.close()
            }
            assertEquals(setOf("alpha.kt", "beta.kt"), ktNames)

            val filteredNames = mutableSetOf<String>()
            val s3 = Files.newDirectoryStream(dir, DirectoryStream.Filter { entry ->
                entry.getFileName()?.toString()?.endsWith(".md") == true
            })
            try {
                for (p in s3) {
                    filteredNames.add(p.getFileName()!!.toString())
                }
            } finally {
                s3.close()
            }
            assertEquals(setOf("notes.md"), filteredNames)

            val fs = FileSystems.getDefault()
            val globMatcher: PathMatcher = fs.getPathMatcher("glob:**/*.{kt,java}")
            assertTrue(globMatcher.matches(Paths.get("src/main/App.kt")))
            assertTrue(globMatcher.matches(Paths.get("src/App.java")))
            assertFalse(globMatcher.matches(Paths.get("App.java")))
            assertFalse(globMatcher.matches(Paths.get("README.md")))

            val regexMatcher: PathMatcher = fs.getPathMatcher("regex:^a.*\\.kt$")
            assertTrue(regexMatcher.matches(Paths.get("alpha.kt")))
            assertFalse(regexMatcher.matches(Paths.get("beta.kt")))

            val customMatcher = PathMatcher { path -> path.toString().endsWith(".md") }
            assertTrue(customMatcher.matches(Paths.get("README.md")))
            assertFalse(customMatcher.matches(Paths.get("App.kt")))

            assertFailsWith<DirectoryNotEmptyException> {
                Files.delete(dir)
            }
        } finally {
            Files.deleteIfExists(f1)
            Files.deleteIfExists(f2)
            Files.deleteIfExists(f3)
            Files.deleteIfExists(hidden)
            Files.deleteIfExists(dir)
        }
    }
}

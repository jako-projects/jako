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
        val sink = pipe.sink()
        val source = pipe.source()
        try {
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
        } finally {
            sink.close()
            source.close()
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
            val globMatcher = fs.getPathMatcher("glob:**/*.{kt,java}")
            assertTrue(globMatcher.matches(Paths.get("src/main/App.kt")))
            assertTrue(globMatcher.matches(Paths.get("src/App.java")))
            assertFalse(globMatcher.matches(Paths.get("App.java")))
            assertFalse(globMatcher.matches(Paths.get("README.md")))

            val regexMatcher = fs.getPathMatcher("regex:^a.*\\.kt$")
            assertTrue(regexMatcher.matches(Paths.get("alpha.kt")))
            assertFalse(regexMatcher.matches(Paths.get("beta.kt")))

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

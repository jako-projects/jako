package jako.test.nio.file

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilesTest {

    @Test
    fun testFileReadWriteAndMetadata() {
        val tempDir = Paths.get("build", "tmp", "test_files_dir")
        Files.createDirectories(tempDir)
        assertTrue(Files.exists(tempDir))
        assertTrue(Files.isDirectory(tempDir))

        val testFile = tempDir / "sample.txt"
        testFile.writeText("Hello Jako NIO!")
        assertTrue(Files.exists(testFile))
        assertTrue(Files.isRegularFile(testFile))
        assertEquals(15L, Files.size(testFile))

        val content = testFile.readText()
        assertEquals("Hello Jako NIO!", content)

        val lines = Files.readAllLines(testFile)
        assertEquals(1, lines.size)
        assertEquals("Hello Jako NIO!", lines[0])

        val copyFile = tempDir / "sample_copy.txt"
        Files.copy(testFile, copyFile, StandardCopyOption.REPLACE_EXISTING)
        assertTrue(Files.exists(copyFile))
        assertEquals("Hello Jako NIO!", copyFile.readText())

        Files.deleteIfExists(testFile)
        Files.deleteIfExists(copyFile)
        assertFalse(Files.exists(testFile))
        assertFalse(Files.exists(copyFile))
    }
}

package jako.test.nio.file

import java.lang.System
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.io.path.div
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileTreeAndAttributesTest {

    @Test
    fun testDefaultFileSystem() {
        val fs = FileSystems.getDefault()
        assertTrue(fs.isOpen())
        val path = fs.getPath("dir", "file.txt")
        assertEquals(
            Paths.get("dir", "file.txt").toString().replace('\\', '/'),
            path.toString().replace('\\', '/')
        )
        assertEquals(fs, path.getFileSystem())
        assertFailsWith<UnsupportedOperationException> { fs.close() }
        assertTrue(fs.isOpen())
    }

    @Test
    fun testFileTimeFromMillisRoundtrip() {
        val time = FileTime.fromMillis(1_234_567L)
        assertEquals(1_234_567L, time.toMillis())
        assertTrue(time.compareTo(FileTime.fromMillis(1_234_568L)) < 0)
        assertEquals(0, time.compareTo(FileTime.fromMillis(1_234_567L)))
        assertEquals(FileTime.fromMillis(1_234_567L), time)
    }

    @Test
    fun testWalkFileTreeAndReadAttributes() {
        val root = Paths.get("build", "tmp", "walk_tree_${System.currentTimeMillis()}")
        val nested = root / "sub"
        val fileA = root / "a.txt"
        val fileB = nested / "b.txt"
        try {
            Files.createDirectories(nested)
            Files.write(fileA, "hello".encodeToByteArray())
            Files.write(fileB, "world".encodeToByteArray())

            val visitedFiles = mutableListOf<String>()
            val visitedDirs = mutableListOf<String>()
            Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    visitedDirs.add(dir.getFileName()?.toString() ?: dir.toString())
                    assertTrue(attrs.isDirectory())
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    visitedFiles.add(file.getFileName().toString())
                    assertTrue(attrs.isRegularFile())
                    return FileVisitResult.CONTINUE
                }
            })

            assertTrue(visitedFiles.contains("a.txt"))
            assertTrue(visitedFiles.contains("b.txt"))
            assertTrue(visitedDirs.isNotEmpty())

            val attrs = Files.readAttributes(
                fileA,
                "basic:size,lastModifiedTime,isDirectory,isRegularFile,isSymbolicLink"
            )
            assertEquals(5L, attrs["size"] as Long)
            assertEquals(false, attrs["isDirectory"])
            assertEquals(true, attrs["isRegularFile"])
            assertEquals(false, attrs["isSymbolicLink"])
            val mtime = attrs["lastModifiedTime"] as FileTime
            val now = System.currentTimeMillis()
            assertTrue(mtime.toMillis() > 0L)
            assertTrue(kotlin.math.abs(mtime.toMillis() - now) < 120_000L)

            val typed = Files.getLastModifiedTime(fileA)
            assertTrue(typed.toMillis() > 0L)
        } finally {
            Files.deleteIfExists(fileB)
            Files.deleteIfExists(nested)
            Files.deleteIfExists(fileA)
            Files.deleteIfExists(root)
        }
    }
}

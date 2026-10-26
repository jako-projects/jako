package jako.test.nio.file

import java.io.IOException
import java.lang.System
import java.nio.file.FileSystems
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.div
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
        val skippedDir = root / "skipped"
        val fileA = root / "a.txt"
        val fileB = nested / "b.txt"
        val fileSkipped = skippedDir / "ignored.txt"
        try {
            Files.createDirectories(nested)
            Files.createDirectories(skippedDir)
            Files.write(fileA, "hello".encodeToByteArray())
            Files.write(fileB, "world".encodeToByteArray())
            Files.write(fileSkipped, "skip".encodeToByteArray())

            val visitedFiles = mutableListOf<String>()
            val visitedDirs = mutableListOf<String>()
            val postDirs = mutableListOf<String>()
            Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    val name = dir.getFileName()?.toString() ?: dir.toString()
                    if (name == "skipped") {
                        return FileVisitResult.SKIP_SUBTREE
                    }
                    visitedDirs.add(name)
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

                override fun postVisitDirectory(
                    dir: Path,
                    exc: IOException?
                ): FileVisitResult {
                    assertNull(exc)
                    postDirs.add(dir.getFileName()?.toString() ?: dir.toString())
                    return FileVisitResult.CONTINUE
                }
            })

            assertTrue(visitedFiles.contains("a.txt"))
            assertTrue(visitedFiles.contains("b.txt"))
            assertFalse(visitedFiles.contains("ignored.txt"))
            assertFalse(postDirs.contains("skipped"))
            assertTrue(visitedDirs.isNotEmpty())

            // Direct FileVisitor implementation + FileVisitOption + maxDepth
            assertEquals(FileVisitOption.FOLLOW_LINKS, FileVisitOption.valueOf("FOLLOW_LINKS"))
            assertEquals(LinkOption.NOFOLLOW_LINKS, LinkOption.valueOf("NOFOLLOW_LINKS"))

            val depthZeroVisited = mutableListOf<Path>()
            val directVisitor = object : FileVisitor<Path> {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
                    FileVisitResult.CONTINUE

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    depthZeroVisited.add(file)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult =
                    FileVisitResult.TERMINATE

                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult =
                    FileVisitResult.CONTINUE
            }
            Files.walkFileTree(root, setOf(FileVisitOption.FOLLOW_LINKS), 0, directVisitor)
            assertEquals(listOf(root), depthZeroVisited)

            assertFailsWith<IllegalArgumentException> {
                Files.walkFileTree(root, emptySet(), -1, directVisitor)
            }

            // visitFileFailed on non-existent path
            var failedCalled = false
            val missingPath = root / "does_not_exist.txt"
            Files.walkFileTree(missingPath, object : FileVisitor<Path> {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
                    FileVisitResult.CONTINUE

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
                    FileVisitResult.CONTINUE

                override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                    failedCalled = true
                    return FileVisitResult.TERMINATE
                }

                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult =
                    FileVisitResult.CONTINUE
            })
            assertTrue(failedCalled)

            // LinkOption.NOFOLLOW_LINKS checks
            assertTrue(Files.exists(fileA, LinkOption.NOFOLLOW_LINKS))
            assertFalse(Files.notExists(fileA, LinkOption.NOFOLLOW_LINKS))
            assertTrue(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS))
            assertTrue(Files.isRegularFile(fileA, LinkOption.NOFOLLOW_LINKS))

            val attrs = Files.readAttributes(
                fileA,
                "basic:size,lastModifiedTime,isDirectory,isRegularFile,isSymbolicLink",
                LinkOption.NOFOLLOW_LINKS
            )
            assertEquals(5L, attrs["size"] as Long)
            assertEquals(false, attrs["isDirectory"])
            assertEquals(true, attrs["isRegularFile"])
            assertEquals(false, attrs["isSymbolicLink"])
            val mtime = attrs["lastModifiedTime"] as FileTime
            val now = System.currentTimeMillis()
            assertTrue(mtime.toMillis() > 0L)
            assertTrue(kotlin.math.abs(mtime.toMillis() - now) < 120_000L)

            val typed = Files.getLastModifiedTime(fileA, LinkOption.NOFOLLOW_LINKS)
            assertTrue(typed.toMillis() > 0L)

            // FileAttribute via PosixFilePermissions.asFileAttribute & custom FileAttribute
            val permSet = PosixFilePermissions.fromString("rw-r-----")
            val posixAttr: FileAttribute<Set<PosixFilePermission>> = PosixFilePermissions.asFileAttribute(permSet)
            assertEquals("posix:permissions", posixAttr.name())
            assertEquals(permSet, posixAttr.value())

            val customAttr = object : FileAttribute<String> {
                override fun name(): String = "user:custom"
                override fun value(): String = "val"
            }
            assertEquals("user:custom", customAttr.name())
            assertEquals("val", customAttr.value())

            val isWindows = System.getProperty("os.name")?.lowercase()?.contains("win") == true
            if (!isWindows) {
                val attrFile = root / "attr_file.txt"
                try {
                    Files.createFile(attrFile, posixAttr)
                    val actualPerms = Files.getPosixFilePermissions(attrFile, LinkOption.NOFOLLOW_LINKS)
                    assertEquals(permSet, actualPerms)
                } finally {
                    Files.deleteIfExists(attrFile)
                }
            }
        } finally {
            Files.deleteIfExists(fileSkipped)
            Files.deleteIfExists(skippedDir)
            Files.deleteIfExists(fileB)
            Files.deleteIfExists(nested)
            Files.deleteIfExists(fileA)
            Files.deleteIfExists(root)
        }
    }
}

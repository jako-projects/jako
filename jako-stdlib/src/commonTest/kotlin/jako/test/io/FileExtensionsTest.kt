package jako.test.io

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileExtensionsTest {

    @Test
    fun testFileExtensionAndNameWithoutExtension() {
        val file = File("/path/to/archive.tar.gz")
        assertEquals("gz", file.extension)
        assertEquals("archive.tar", file.nameWithoutExtension)

        val file2 = File("document.txt")
        assertEquals("txt", file2.extension)
        assertEquals("document", file2.nameWithoutExtension)

        val file3 = File("no_extension")
        assertEquals("", file3.extension)
        assertEquals("no_extension", file3.nameWithoutExtension)
    }

    private val tmpBase: File
        get() = File(java.lang.System.getProperty("java.io.tmpdir") ?: "/tmp")

    @Test
    fun testFileReadWriteTextAndBytes() {
        val tempFile = File(tmpBase, "jako_file_ext_test_" + java.lang.System.currentTimeMillis() + ".txt")
        try {
            tempFile.writeText("Hello File Text")
            assertEquals("Hello File Text", tempFile.readText())

            tempFile.appendText("\nAppended Line")
            val lines = tempFile.readLines()
            assertEquals(2, lines.size)
            assertEquals("Hello File Text", lines[0])
            assertEquals("Appended Line", lines[1])

            val bytes = byteArrayOf(10, 20, 30, 40)
            tempFile.writeBytes(bytes)
            val readBack = tempFile.readBytes()
            assertEquals(4, readBack.size)
            assertEquals(10, readBack[0].toInt())
            assertEquals(40, readBack[3].toInt())

            tempFile.appendBytes(byteArrayOf(50, 60))
            val readBack2 = tempFile.readBytes()
            assertEquals(6, readBack2.size)
            assertEquals(60, readBack2[5].toInt())
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun testFileCopyToAndDeleteRecursively() {
        val srcFile = File(tmpBase, "jako_src_" + java.lang.System.currentTimeMillis() + ".txt")
        val dstFile = File(tmpBase, "jako_dst_" + java.lang.System.currentTimeMillis() + ".txt")
        try {
            srcFile.writeText("Copy Content")
            srcFile.copyTo(dstFile, overwrite = true)
            assertTrue(dstFile.exists())
            assertEquals("Copy Content", srcFile.readText())
        } finally {
            srcFile.delete()
            dstFile.delete()
        }

        val dir = File(tmpBase, "jako_dir_test_" + java.lang.System.currentTimeMillis())
        val subDir = File(dir, "subdir")
        val childFile = File(subDir, "child.txt")
        subDir.mkdirs()
        childFile.writeText("Child Content")

        assertTrue(childFile.exists())
        val deleted = dir.deleteRecursively()
        assertTrue(deleted)
        assertFalse(dir.exists())
    }

    @Test
    fun testFileWalkAndCopyRecursively() {
        val srcDir = File(tmpBase, "jako_walk_src_" + java.lang.System.currentTimeMillis())
        val dstDir = File(tmpBase, "jako_walk_dst_" + java.lang.System.currentTimeMillis())
        try {
            val subA = File(srcDir, "a")
            val subB = File(subA, "b")
            subB.mkdirs()
            val file1 = File(srcDir, "root.txt")
            val file2 = File(subA, "a.txt")
            val file3 = File(subB, "b.txt")
            file1.writeText("root")
            file2.writeText("a")
            file3.writeText("b")

            val entered = mutableListOf<String>()
            val left = mutableListOf<String>()
            val topDownWalk: FileTreeWalk = srcDir.walk(FileWalkDirection.TOP_DOWN)
                .onEnter { dir -> entered.add(dir.name); true }
                .onLeave { dir -> left.add(dir.name) }
                .onFail { _, _ -> }
            val topDownNames = topDownWalk.map { it.name }.toList()
            assertEquals(srcDir.name, topDownNames.first())
            assertEquals(6, topDownNames.size)
            assertTrue(entered.contains("a"))
            assertTrue(left.contains("b"))

            val shallowNames = srcDir.walk(FileWalkDirection.TOP_DOWN).maxDepth(1).map { it.name }.toSet()
            assertEquals(setOf(srcDir.name, "a", "root.txt"), shallowNames)
            kotlin.test.assertFailsWith<IllegalArgumentException> {
                srcDir.walk(FileWalkDirection.TOP_DOWN).maxDepth(0)
            }

            val bottomUpNames = srcDir.walk(FileWalkDirection.BOTTOM_UP).map { it.name }.toList()
            assertEquals(srcDir.name, bottomUpNames.last())
            assertEquals(6, bottomUpNames.size)

            assertTrue(srcDir.copyRecursively(dstDir, overwrite = true))
            assertTrue(File(dstDir, "root.txt").exists())
            assertEquals("b", File(File(File(dstDir, "a"), "b"), "b.txt").readText())

            // Copy without overwrite using OnErrorAction.SKIP and OnErrorAction.TERMINATE
            var skipConflicts = 0
            assertTrue(
                srcDir.copyRecursively(dstDir, overwrite = false) { _, _ ->
                    skipConflicts++
                    OnErrorAction.SKIP
                }
            )
            assertTrue(skipConflicts > 0)

            assertFalse(
                srcDir.copyRecursively(dstDir, overwrite = false) { _, _ ->
                    OnErrorAction.TERMINATE
                }
            )
        } finally {
            srcDir.deleteRecursively()
            dstDir.deleteRecursively()
        }
    }

    @Test
    fun testFileRelativeToAndStartsEndsWith() {
        val base = File("/usr/local/bin")
        val target = File("/usr/local/lib/libfoo.so")
        assertEquals(File("../lib/libfoo.so"), target.relativeTo(base))
        assertEquals(File("libfoo.so"), target.relativeTo(File("/usr/local/lib")))

        assertTrue(target.startsWith(File("/usr/local")))
        assertTrue(target.startsWith("/usr"))
        assertFalse(target.startsWith("/var"))

        assertTrue(target.endsWith(File("lib/libfoo.so")))
        assertTrue(target.endsWith("libfoo.so"))
        assertFalse(target.endsWith("other.so"))
    }
}


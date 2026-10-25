package jako.test.io

import java.io.*
import java.lang.System

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FileTest {

    @Test
    fun testFileNamesAndPaths() {
        val file = File("/tmp/jako_test_file.txt")
        assertEquals("/tmp/jako_test_file.txt", file.getPath().replace('\\', '/'))
        assertEquals("jako_test_file.txt", file.getName())
        assertEquals("/tmp", file.getParent()?.replace('\\', '/'))
        assertNotNull(file.getParentFile())
        assertEquals("/tmp", file.getParentFile()?.getPath()?.replace('\\', '/'))
    }

    @Test
    fun testFileParentChildConstructor() {
        val parent = File("/tmp")
        val file = File(parent, "child.txt")
        assertEquals("/tmp/child.txt", file.getPath().replace('\\', '/'))
        assertEquals("child.txt", file.getName())
    }

    @Test
    fun testNonExistentFileBehavior() {
        val file = File("/tmp/non_existent_jako_file_12345.txt")
        assertFalse(file.exists())
        assertFalse(file.isDirectory())
        assertFalse(file.isFile())
        assertEquals(0L, file.length())
        assertFalse(file.delete())
        assertNull(file.list())
        assertNull(file.listFiles())
    }

    private val tmpBase: File
        get() = File(System.getProperty("java.io.tmpdir") ?: "/tmp")

    @Test
    fun testDirectoryCreationAndDeletion() {
        val dir = File(tmpBase, "jako_test_dir_${System.currentTimeMillis()}")
        if (dir.exists()) {
            dir.delete()
        }
        assertFalse(dir.exists())

        val created = dir.mkdir()
        assertTrue(created)
        assertTrue(dir.exists())
        assertTrue(dir.isDirectory())
        assertFalse(dir.isFile())

        val deleted = dir.delete()
        assertTrue(deleted)
        assertFalse(dir.exists())
    }

    @Test
    fun testCreateNewFileLastModifiedAndRename() {
        val base = File(tmpBase, "jako_file_ops_${System.currentTimeMillis()}")
        if (base.exists()) base.delete()
        assertTrue(base.createNewFile())
        assertFalse(base.createNewFile())
        assertTrue(base.exists())
        assertTrue(base.canRead())
        assertTrue(base.canWrite())
        assertFalse(base.isHidden())

        val now = System.currentTimeMillis()
        val mtime = base.lastModified()
        assertTrue(mtime > 0L, "mtime=$mtime now=$now")
        assertTrue(kotlin.math.abs(mtime - now) < 120_000L, "mtime=$mtime now=$now")

        val stamp = 1_700_000_000_000L
        assertTrue(base.setLastModified(stamp), "setLastModified failed")
        val afterSet = base.lastModified()
        assertTrue(kotlin.math.abs(afterSet - stamp) < 2_000L, "afterSet=$afterSet stamp=$stamp")

        val renamed = File(tmpBase, "jako_file_ops_renamed_${System.currentTimeMillis()}.txt")
        if (renamed.exists()) renamed.delete()
        assertTrue(base.renameTo(renamed))
        assertFalse(base.exists())
        assertTrue(renamed.exists())

        val canonical = renamed.getCanonicalFile()
        assertTrue(canonical.isAbsolute())
        assertTrue(canonical.exists())
        renamed.delete()
    }

    @Test
    fun testListFiltersAndSeparators() {
        assertTrue(File.separator.isNotEmpty())
        assertTrue(File.pathSeparator.isNotEmpty())

        val dir = File(tmpBase, "jako_list_filter_${System.currentTimeMillis()}")
        if (dir.exists()) dir.delete()
        assertTrue(dir.mkdir())
        val keep = File(dir, "keep.txt")
        val skip = File(dir, "skip.bin")
        assertTrue(keep.createNewFile())
        assertTrue(skip.createNewFile())
        try {
            val names = dir.list { _, name -> name.endsWith(".txt") }
            assertNotNull(names)
            assertEquals(1, names!!.size)
            assertEquals("keep.txt", names[0])

            val files = dir.listFiles { file -> file.getName().endsWith(".txt") }
            assertNotNull(files)
            assertEquals(1, files!!.size)
            assertEquals("keep.txt", files[0].getName())
        } finally {
            keep.delete()
            skip.delete()
            dir.delete()
        }
    }

    @Test
    fun testCreateTempFile() {
        val tmpDirProp = System.getProperty("java.io.tmpdir")
        assertNotNull(tmpDirProp)
        assertTrue(tmpDirProp!!.isNotEmpty())

        val tempFile = File.createTempFile("jako_temp_", ".test")
        try {
            assertTrue(tempFile.exists())
            assertTrue(tempFile.isFile())
            assertTrue(tempFile.getName().startsWith("jako_temp_"))
            assertTrue(tempFile.getName().endsWith(".test"))
        } finally {
            tempFile.delete()
            assertFalse(tempFile.exists())
        }

        // Test default suffix
        val tempFileDefaultSuffix = File.createTempFile("jako_temp_", null)
        try {
            assertTrue(tempFileDefaultSuffix.exists())
            assertTrue(tempFileDefaultSuffix.getName().endsWith(".tmp"))
        } finally {
            tempFileDefaultSuffix.delete()
        }

        // Test short prefix (< 3 chars) throws IllegalArgumentException
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            File.createTempFile("ab", ".txt")
        }
    }
}

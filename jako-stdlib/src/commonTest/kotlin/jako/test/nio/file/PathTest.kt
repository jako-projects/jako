package jako.test.nio.file

import java.nio.file.Paths
import kotlin.io.path.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathTest {

    @Test
    fun testPathComponentsAndResolution() {
        val path = Paths.get("dir", "sub", "file.txt")
        assertEquals("file.txt", path.getFileName()?.toString())
        assertEquals("dir/sub", path.getParent()?.toString()?.replace('\\', '/'))
        assertEquals("txt", path.extension)
        assertEquals("file", path.nameWithoutExtension)

        val child = path / "extra"
        assertEquals("dir/sub/file.txt/extra", child.toString().replace('\\', '/'))
    }

    @Test
    fun testPathNormalization() {
        val path = Paths.get("a", "b", "..", "c", ".", "file.txt")
        val normalized = path.normalize()
        assertEquals("a/c/file.txt", normalized.toString().replace('\\', '/'))
    }

    @Test
    fun testStartsAndEndsWith() {
        val path = Paths.get("alpha", "beta", "gamma.log")
        assertTrue(path.startsWith("alpha"))
        assertTrue(path.startsWith("alpha/beta"))
        assertTrue(path.endsWith("gamma.log"))
        assertTrue(path.endsWith("beta/gamma.log"))
        assertFalse(path.startsWith("beta"))
    }
}

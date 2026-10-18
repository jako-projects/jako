package jako.android.test.net

import android.net.Uri
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UriTest {

    @Test
    fun testHierarchicalUriParsing() {
        val uri = Uri.parse("https://user:pass@example.com:8443/api/v1/items%20list?tag=kotlin&tag=native&active=true&empty=#sec-1")

        assertTrue(uri.isHierarchical)
        assertFalse(uri.isOpaque)
        assertTrue(uri.isAbsolute)
        assertFalse(uri.isRelative)

        assertEquals("https", uri.scheme)
        assertEquals("user:pass@example.com:8443", uri.authority)
        assertEquals("user:pass", uri.userInfo)
        assertEquals("example.com", uri.host)
        assertEquals(8443, uri.port)

        assertEquals("/api/v1/items%20list", uri.encodedPath)
        assertEquals("/api/v1/items list", uri.path)
        assertEquals(listOf("api", "v1", "items list"), uri.pathSegments)
        assertEquals("items list", uri.lastPathSegment)

        assertEquals("tag=kotlin&tag=native&active=true&empty=", uri.encodedQuery)
        assertEquals(setOf("tag", "active", "empty"), uri.queryParameterNames)
        assertEquals("kotlin", uri.getQueryParameter("tag"))
        assertEquals(listOf("kotlin", "native"), uri.getQueryParameters("tag"))
        assertEquals("", uri.getQueryParameter("empty"))
        assertNull(uri.getQueryParameter("missing"))
        assertTrue(uri.getBooleanQueryParameter("active", false))
        assertFalse(uri.getBooleanQueryParameter("missing", false))

        assertEquals("sec-1", uri.fragment)
    }

    @Test
    fun testOpaqueUri() {
        val mailto = Uri.parse("mailto:john.doe@example.com#work")
        assertFalse(mailto.isHierarchical)
        assertTrue(mailto.isOpaque)
        assertTrue(mailto.isAbsolute)
        assertEquals("mailto", mailto.scheme)
        assertEquals("john.doe@example.com", mailto.schemeSpecificPart)
        assertNull(mailto.authority)
        assertNull(mailto.path)
        assertNull(mailto.query)
        assertEquals(-1, mailto.port)
        assertEquals("work", mailto.fragment)

        assertFailsWith<UnsupportedOperationException> {
            mailto.queryParameterNames
        }

        val fromParts = Uri.fromParts("tel", "+1-800-555-0199", "ext123")
        assertTrue(fromParts.isOpaque)
        assertEquals("tel", fromParts.scheme)
        assertEquals("+1-800-555-0199", fromParts.schemeSpecificPart)
        assertEquals("ext123", fromParts.fragment)
    }

    @Test
    fun testBuilderAndTransformations() {
        val built = Uri.Builder()
            .scheme("HTTP")
            .authority("api.example.com")
            .appendPath("search")
            .appendPath("a/b")
            .appendQueryParameter("q", "hello world")
            .appendQueryParameter("page", "1")
            .fragment("top")
            .build()

        assertEquals("HTTP://api.example.com/search/a%2Fb?q=hello%20world&page=1#top", built.toString())
        assertEquals(listOf("search", "a/b"), built.pathSegments)
        assertEquals("hello world", built.getQueryParameter("q"))

        val normalized = built.normalizeScheme()
        assertEquals("http", normalized.scheme)

        val rebuilt = normalized.buildUpon()
            .clearQuery()
            .appendQueryParameter("page", "2")
            .build()
        assertEquals("http://api.example.com/search/a%2Fb?page=2#top", rebuilt.toString())

        val appended = Uri.withAppendedPath(Uri.parse("content://com.example.provider/items"), "42")
        assertEquals("content://com.example.provider/items/42", appended.toString())
    }

    @Test
    fun testEncodeDecodeAndEmpty() {
        assertNull(Uri.encode(null))
        assertNull(Uri.decode(null))
        assertEquals("hello%20world%2Ftest", Uri.encode("hello world/test"))
        assertEquals("hello%20world/test", Uri.encode("hello world/test", "/"))
        assertEquals("hello world/test", Uri.decode("hello%20world%2Ftest"))
        assertEquals("中文测试", Uri.decode(Uri.encode("中文测试")))

        assertEquals("", Uri.EMPTY.toString())
        assertTrue(Uri.EMPTY.isRelative)
        assertTrue(Uri.EMPTY.pathSegments.isEmpty())
        assertNull(Uri.EMPTY.lastPathSegment)

        val fileUri = Uri.fromFile(File("/tmp/test file.txt"))
        assertEquals("file", fileUri.scheme)
        assertEquals("/tmp/test file.txt", fileUri.path)
    }
}

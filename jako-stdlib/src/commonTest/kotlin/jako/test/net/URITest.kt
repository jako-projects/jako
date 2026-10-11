package jako.test.net

import java.net.*

import kotlin.test.*

class URITest {

    @Test
    fun testURIParseAndProperties() {
        val uri = URI("https://github.com:443/jako-projects/jako?view=main#readme")

        assertEquals("https", uri.getScheme())
        assertEquals("github.com:443", uri.getAuthority())
        assertEquals("github.com", uri.getHost())
        assertEquals(443, uri.getPort())
        assertEquals("/jako-projects/jako", uri.getPath())
        assertEquals("view=main", uri.getQuery())
        assertEquals("readme", uri.getFragment())
        assertTrue(uri.isAbsolute())
        assertFalse(uri.isOpaque())
    }

    @Test
    fun testRelativeAndOpaqueURI() {
        val relative = URI("/path/to/resource?query=1")
        assertFalse(relative.isAbsolute())
        assertEquals("/path/to/resource", relative.getPath())

        val opaque = URI("mailto:user@example.com")
        assertTrue(opaque.isAbsolute())
        assertTrue(opaque.isOpaque())
    }

    @Test
    fun testMultiArgConstructor() {
        val uri = URI("https", "user:pass@example.com:8080", "/api/v1", "key=val", "sec1")

        assertEquals("https", uri.getScheme())
        assertEquals("user:pass@example.com:8080", uri.getAuthority())
        assertEquals("example.com", uri.getHost())
        assertEquals(8080, uri.getPort())
        assertEquals("/api/v1", uri.getPath())
        assertEquals("key=val", uri.getQuery())
        assertEquals("sec1", uri.getFragment())
    }

    @Test
    fun testURIResolve() {
        val base = URI("https://github.com/jako-projects/jako/")
        val resolved = base.resolve("issues")
        assertEquals("https://github.com/jako-projects/jako/issues", resolved.toString())

        val resolvedAbs = base.resolve("https://other.com/test")
        assertEquals("https://other.com/test", resolvedAbs.toString())
    }

    @Test
    fun testURIEqualsHashCodeCompareTo() {
        val u1 = URI.create("https://example.com/a")
        val u2 = URI("https://example.com/a")
        val u3 = URI("https://example.com/b")

        assertEquals(u1, u2)
        assertEquals(u1.hashCode(), u2.hashCode())
        assertTrue(u1 < u3)
    }

    @Test
    fun testURL() {
        val url = URL("https", "github.com", 443, "/jako-projects/jako")

        assertEquals("https", url.getProtocol())
        assertEquals("github.com", url.getHost())
        assertEquals(443, url.getPort())
        assertEquals("/jako-projects/jako", url.getPath())
        assertEquals("https://github.com:443/jako-projects/jako", url.toURI().toString())

        val url2 = URL("https://github.com:443/jako-projects/jako")
        assertEquals(url, url2)
    }
}

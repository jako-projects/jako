package jako.android.test.webkit

import android.webkit.URLUtil
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class URLUtilTest {

    @Test
    fun testUrlPredicates() {
        assertTrue(URLUtil.isHttpUrl("http://example.com"))
        assertTrue(URLUtil.isHttpUrl("HTTP://example.com/path"))
        assertFalse(URLUtil.isHttpUrl("https://example.com"))
        assertFalse(URLUtil.isHttpUrl("http:/"))
        assertFalse(URLUtil.isHttpUrl(null))

        assertTrue(URLUtil.isHttpsUrl("https://example.com"))
        assertTrue(URLUtil.isHttpsUrl("HTTPS://example.com/path"))
        assertFalse(URLUtil.isHttpsUrl("http://example.com"))
        assertFalse(URLUtil.isHttpsUrl("https:/"))

        assertTrue(URLUtil.isNetworkUrl("http://example.com"))
        assertTrue(URLUtil.isNetworkUrl("https://example.com"))
        assertFalse(URLUtil.isNetworkUrl("file:///tmp/a.txt"))
        assertFalse(URLUtil.isNetworkUrl(null))

        assertTrue(URLUtil.isAssetUrl("file:///android_asset/index.html"))
        assertFalse(URLUtil.isAssetUrl("file:///sdcard/index.html"))

        assertTrue(URLUtil.isFileUrl("file:///sdcard/index.html"))
        assertFalse(URLUtil.isFileUrl("file:///android_asset/index.html"))

        assertTrue(URLUtil.isAboutUrl("about:blank"))
        assertTrue(URLUtil.isDataUrl("data:text/plain;base64,SGVsbG8="))
        assertTrue(URLUtil.isJavaScriptUrl("javascript:alert(1)"))
        assertTrue(URLUtil.isContentUrl("content://media/external/images/media"))

        assertTrue(URLUtil.isValidUrl("https://example.com"))
        assertTrue(URLUtil.isValidUrl("file:///tmp/test"))
        assertFalse(URLUtil.isValidUrl("not a url"))
        assertFalse(URLUtil.isValidUrl(""))
        assertFalse(URLUtil.isValidUrl(null))
    }

    @Test
    fun testStripAnchorAndComposeSearchUrl() {
        assertEquals("https://example.com/page", URLUtil.stripAnchor("https://example.com/page#section1"))
        assertEquals("https://example.com/page", URLUtil.stripAnchor("https://example.com/page"))
        assertNull(URLUtil.stripAnchor(null))

        assertEquals(
            "https://www.google.com/search?q=kotlin%20multiplatform&ie=utf-8",
            URLUtil.composeSearchUrl(
                "kotlin multiplatform",
                "https://www.google.com/search?q=%s&ie=utf-8",
                "%s"
            )
        )
        assertNull(URLUtil.composeSearchUrl("test", "https://example.com", "%s"))
    }

    @Test
    fun testGuessUrlAndDecode() {
        assertEquals("http://example.com", URLUtil.guessUrl("example.com."))
        assertEquals("https://example.com/a", URLUtil.guessUrl("https://example.com/a"))
        assertEquals("about:blank", URLUtil.guessUrl("about:blank"))

        val encoded = "hello%20world%21".encodeToByteArray()
        assertContentEquals("hello world!".encodeToByteArray(), URLUtil.decode(encoded))
        assertNull(URLUtil.decode(null))
        assertContentEquals(ByteArray(0), URLUtil.decode(ByteArray(0)))
        assertFailsWith<IllegalArgumentException> {
            URLUtil.decode("abc%2".encodeToByteArray())
        }
        assertFailsWith<IllegalArgumentException> {
            URLUtil.decode("abc%ZZ".encodeToByteArray())
        }
    }

    @Test
    fun testGuessFileName() {
        assertEquals(
            "report.pdf",
            URLUtil.guessFileName("https://example.com/files/report.pdf?token=123", null, "application/pdf")
        )
        assertEquals(
            "custom_name.zip",
            URLUtil.guessFileName(
                "https://example.com/download?id=99",
                "attachment; filename=\"custom_name.zip\"",
                "application/zip"
            )
        )
        assertEquals(
            "image.png",
            URLUtil.guessFileName("https://example.com/assets/image", null, "image/png")
        )
        assertEquals(
            "downloadfile.html",
            URLUtil.guessFileName("https://example.com/view/", null, "text/html")
        )
        assertEquals(
            "notes.txt",
            URLUtil.guessFileName("https://example.com/notes", null, "text/plain")
        )
        assertEquals(
            "archive.zip",
            URLUtil.guessFileName("https://example.com/archive.txt", null, "application/zip")
        )
        assertEquals(
            "downloadfile.bin",
            URLUtil.guessFileName(null, null, null)
        )
    }
}

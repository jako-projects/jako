package jako.test.net

import java.net.*

import kotlin.test.*

class URLEncodingTest {

    @Test
    fun testURLEncodeAndDecodeStandard() {
        val original = "Hello World! 编程 & 测试 = 100%"
        val encoded = URLEncoder.encode(original, "UTF-8")

        assertTrue(encoded.contains("+"), "Spaces should be encoded as '+'")
        assertTrue(encoded.contains("%"), "Non-ASCII chars should be percent-encoded")

        val decoded = URLDecoder.decode(encoded, "UTF-8")
        assertEquals(original, decoded)
    }

    @Test
    fun testUnreservedCharacters() {
        val unreserved = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._*"
        val encoded = URLEncoder.encode(unreserved, "UTF-8")

        assertEquals(unreserved, encoded, "Unreserved characters should remain unencoded")
    }

    @Test
    fun testSpaceEncoding() {
        val spaces = "a b c"
        val encoded = URLEncoder.encode(spaces, "UTF-8")

        assertEquals("a+b+c", encoded)
        assertEquals(spaces, URLDecoder.decode(encoded, "UTF-8"))
    }

    @Test
    fun testEmojiAndMultiByteEncoding() {
        val emoji = "🚀 Multiplatform Kotlin 🚀"
        val encoded = URLEncoder.encode(emoji, "UTF-8")
        val decoded = URLDecoder.decode(encoded, "UTF-8")

        assertEquals(emoji, decoded)
    }
}

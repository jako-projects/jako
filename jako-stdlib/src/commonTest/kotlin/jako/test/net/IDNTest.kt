package jako.test.net

import java.net.IDN
import kotlin.test.Test
import kotlin.test.assertEquals

class IDNTest {

    @Test
    fun testAsciiDomain() {
        val ascii = "example.com"
        assertEquals(ascii, IDN.toASCII(ascii))
        assertEquals(ascii, IDN.toUnicode(ascii))
    }

    @Test
    fun testInternationalizedDomain() {
        val unicodeDomain = "你好.中国"
        val asciiDomain = IDN.toASCII(unicodeDomain)
        val restored = IDN.toUnicode(asciiDomain)
        assertEquals(unicodeDomain, restored)
    }

    @Test
    fun testMixedDomain() {
        val unicodeDomain = "kotlin.中文.org"
        val asciiDomain = IDN.toASCII(unicodeDomain)
        assertEquals("kotlin.xn--fiq228c.org", asciiDomain)

        val restored = IDN.toUnicode(asciiDomain)
        assertEquals(unicodeDomain, restored)
    }
}

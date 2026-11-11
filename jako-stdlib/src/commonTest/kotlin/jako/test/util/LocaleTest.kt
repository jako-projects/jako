package jako.test.util

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LocaleTest {

    @Test
    fun testStandardLocales() {
        assertEquals("en", Locale.US.language)
        assertEquals("US", Locale.US.country)
        assertEquals("en_US", Locale.US.toString())

        assertEquals("zh", Locale.SIMPLIFIED_CHINESE.language)
        assertEquals("CN", Locale.SIMPLIFIED_CHINESE.country)

        assertEquals("zh", Locale.TRADITIONAL_CHINESE.language)
        assertEquals("TW", Locale.TRADITIONAL_CHINESE.country)

        assertEquals("fr", Locale.FRANCE.language)
        assertEquals("FR", Locale.FRANCE.country)
    }

    @Test
    fun testLanguageTag() {
        val loc = Locale.forLanguageTag("zh-CN")
        assertEquals("zh", loc.language)
        assertEquals("CN", loc.country)
        assertEquals("zh-CN", loc.toLanguageTag())
    }

    @Test
    fun testEqualityAndHash() {
        val loc1 = Locale("en", "US")
        val loc2 = Locale.US
        assertEquals(loc1, loc2)
        assertEquals(loc1.hashCode(), loc2.hashCode())

        assertNotEquals(Locale.US, Locale.UK)
    }

    @Test
    fun testRootLocale() {
        assertEquals("", Locale.ROOT.language)
        assertEquals("", Locale.ROOT.country)
        assertEquals("", Locale.ROOT.variant)
        assertEquals("", Locale.ROOT.toString())
        assertEquals("und", Locale.ROOT.toLanguageTag())
    }

    @Test
    fun testVariantLocale() {
        val posix = Locale("en", "US", "POSIX")
        assertEquals("en_US_POSIX", posix.toString())
        assertEquals("en-US-POSIX", posix.toLanguageTag())
        assertNotEquals(Locale.US, posix)
    }

    @Test
    fun testUndeterminedLanguageTag() {
        val empty = Locale.forLanguageTag("")
        assertEquals("", empty.language)
        assertEquals("und", empty.toLanguageTag())

        val und = Locale.forLanguageTag("und")
        assertEquals("", und.language)
        assertEquals("und", und.toLanguageTag())
    }
}

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
}

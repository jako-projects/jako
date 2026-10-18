package jako.android.test.util

import android.util.Base64
import android.util.Log
import android.util.Pair
import android.util.Patterns
import android.util.Range
import android.util.Rational
import android.util.Size
import android.util.SizeF
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UtilBasicTest {

    @Test
    fun testPair() {
        val p1 = Pair("key", 42)
        val p2 = Pair.create("key", 42)
        val p3 = Pair<String?, Int?>(null, null)

        assertEquals("key", p1.first)
        assertEquals(42, p1.second)
        assertEquals(p1, p2)
        assertEquals(p1.hashCode(), p2.hashCode())
        assertEquals("Pair{key 42}", p1.toString())

        assertEquals(0, p3.hashCode())
        assertEquals("Pair{null null}", p3.toString())
        assertNotEquals<Any?>(p1, p3)
    }

    @Test
    fun testSizeAndSizeF() {
        val s1 = Size(1920, 1080)
        assertEquals(1920, s1.width)
        assertEquals(1080, s1.height)
        assertEquals(1920, s1.getWidth())
        assertEquals(1080, s1.getHeight())
        assertEquals("1920x1080", s1.toString())
        assertEquals(s1, Size.parseSize("1920x1080"))
        assertEquals(s1, Size.parseSize("1920*1080"))
        assertFailsWith<NumberFormatException> {
            Size.parseSize("invalid")
        }

        val sf1 = SizeF(12.5f, 25.0f)
        assertEquals(12.5f, sf1.width)
        assertEquals(25.0f, sf1.height)
        assertEquals(sf1, SizeF.parseSizeF("12.5x25.0"))
        assertEquals(sf1, SizeF.parseSizeF("12.5*25.0"))
        assertFailsWith<IllegalArgumentException> {
            SizeF(Float.NaN, 1.0f)
        }
        assertFailsWith<NumberFormatException> {
            SizeF.parseSizeF("NaNx1.0")
        }
    }

    @Test
    fun testRational() {
        val r1 = Rational(2, 4)
        assertEquals(1, r1.numerator)
        assertEquals(2, r1.denominator)
        assertEquals("1/2", r1.toString())
        assertEquals(0.5, r1.toDouble())
        assertTrue(r1.isFinite)
        assertFalse(r1.isZero)
        assertFalse(r1.isNaN)
        assertFalse(r1.isInfinite)

        val neg = Rational(3, -6)
        assertEquals(-1, neg.numerator)
        assertEquals(2, neg.denominator)
        assertEquals("-1/2", neg.toString())

        assertEquals(Rational.ZERO, Rational(0, 99))
        assertTrue(Rational.ZERO.isZero)
        assertEquals(Rational.NaN, Rational(0, 0))
        assertTrue(Rational.NaN.isNaN)
        assertEquals(Rational.POSITIVE_INFINITY, Rational(5, 0))
        assertEquals(Rational.NEGATIVE_INFINITY, Rational(-5, 0))

        assertEquals(r1, Rational.parseRational("2/4"))
        assertEquals(r1, Rational.parseRational("1:2"))
        assertEquals(Rational.NaN, Rational.parseRational("NaN"))
        assertEquals(Rational.POSITIVE_INFINITY, Rational.parseRational("Infinity"))
        assertEquals(Rational.NEGATIVE_INFINITY, Rational.parseRational("-Infinity"))

        assertTrue(Rational(1, 3) < Rational(1, 2))
        assertTrue(Rational.NEGATIVE_INFINITY < Rational(-100, 1))
        assertTrue(Rational(100, 1) < Rational.POSITIVE_INFINITY)
    }

    @Test
    fun testRange() {
        val range = Range.create(10, 20)
        assertEquals(10, range.lower)
        assertEquals(20, range.upper)
        assertTrue(10 in range)
        assertTrue(15 in range)
        assertTrue(20 in range)
        assertFalse(9 in range)
        assertFalse(21 in range)

        assertEquals(10, range.clamp(5))
        assertEquals(15, range.clamp(15))
        assertEquals(20, range.clamp(25))

        assertEquals(Range(12, 18), range.intersect(12, 18))
        assertEquals(Range(15, 20), range.intersect(Range(15, 30)))
        assertFailsWith<IllegalArgumentException> {
            range.intersect(25, 30)
        }

        assertEquals(Range(5, 25), range.extend(5, 25))
        assertEquals(Range(5, 20), range.extend(5))
        assertEquals("[10, 20]", range.toString())
    }

    @Test
    fun testBase64() {
        val raw = "Hello, Jako Android Base64?!".toByteArray(StandardCharsets.UTF_8)

        val defaultEncoded = Base64.encodeToString(raw, Base64.DEFAULT)
        assertTrue(defaultEncoded.endsWith("\n"))
        assertContentEquals(raw, Base64.decode(defaultEncoded, Base64.DEFAULT))

        val noWrap = Base64.encodeToString(raw, Base64.NO_WRAP)
        assertFalse(noWrap.contains("\n"))
        assertEquals("SGVsbG8sIEpha28gQW5kcm9pZCBCYXNlNjQ/IQ==", noWrap)
        assertContentEquals(raw, Base64.decode(noWrap, Base64.NO_WRAP))

        val noPadUrlSafe = Base64.encodeToString(raw, Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE)
        assertEquals("SGVsbG8sIEpha28gQW5kcm9pZCBCYXNlNjQ_IQ", noPadUrlSafe)
        assertContentEquals(raw, Base64.decode(noPadUrlSafe, Base64.URL_SAFE))
    }

    @Test
    fun testPatterns() {
        assertTrue(Patterns.EMAIL_ADDRESS.matcher("dev@example.com").matches())
        assertFalse(Patterns.EMAIL_ADDRESS.matcher("not-an-email").matches())
        assertTrue(Patterns.IP_ADDRESS.matcher("192.168.1.1").matches())
        assertFalse(Patterns.IP_ADDRESS.matcher("999.999.1.1").matches())
        assertTrue(Patterns.DOMAIN_NAME.matcher("github.com").matches())
    }

    @Test
    fun testLogConstantsAndStackTrace() {
        assertEquals(2, Log.VERBOSE)
        assertEquals(3, Log.DEBUG)
        assertEquals(4, Log.INFO)
        assertEquals(5, Log.WARN)
        assertEquals(6, Log.ERROR)
        assertEquals(7, Log.ASSERT)

        assertEquals("", Log.getStackTraceString(null))
        assertEquals("", Log.getStackTraceString(UnknownHostException("offline.example.com")))

        val trace = Log.getStackTraceString(IllegalStateException("boom"))
        assertTrue(trace.contains("IllegalStateException"))
        assertTrue(trace.contains("boom"))
    }
}

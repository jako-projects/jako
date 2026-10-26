package jako.test.text

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.FieldPosition
import java.text.Format
import java.text.NumberFormat
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormatTest {

    @Test
    fun testDecimalFormat() {
        val df = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
        val formatted = df.format(1234567.89)
        assertEquals("1,234,567.89", formatted)

        val pos = ParsePosition(0)
        val parsed = df.parse("1,234,567.89", pos)
        assertEquals(1234567.89, parsed?.toDouble())
    }

    @Test
    fun testNumberFormat() {
        val nf = NumberFormat.getIntegerInstance(Locale.US)
        val formatted = nf.format(123456)
        assertEquals("123,456", formatted)
    }

    @Test
    fun testDecimalFormatLocalesAndPatterns() {
        val usDf = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
        val deDf = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY))
        assertEquals("1,234,567.89", usDf.format(1234567.89))
        assertEquals("1.234.567,89", deDf.format(1234567.89))
        assertEquals(1234567.89, deDf.parse("1.234.567,89").toDouble(), 1e-6)

        // Positive and negative subpatterns
        val accounting = DecimalFormat("#,##0.00;(#,##0.00)", DecimalFormatSymbols.getInstance(Locale.US))
        assertEquals("1,234.50", accounting.format(1234.5))
        assertEquals("(1,234.50)", accounting.format(-1234.5))
        assertEquals(-1234.5, accounting.parse("(1,234.50)").toDouble(), 1e-6)

        // Percent and per-mille
        val pctDf = DecimalFormat("#0.0%", DecimalFormatSymbols.getInstance(Locale.US))
        assertEquals("12.5%", pctDf.format(0.125))
        assertEquals(0.125, pctDf.parse("12.5%").toDouble(), 1e-6)

        // Currency symbol replacement ('\u00A4')
        val curUs = DecimalFormat("\u00A4#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
        assertEquals("$1,234.50", curUs.format(1234.5))
        assertEquals(1234.5, curUs.parse("$1,234.50").toDouble(), 1e-6)

        // RoundingMode support
        val roundDf = DecimalFormat("#0.0", DecimalFormatSymbols.getInstance(Locale.US))
        roundDf.setRoundingMode(RoundingMode.HALF_UP)
        assertEquals(RoundingMode.HALF_UP, roundDf.getRoundingMode())
        assertEquals("1.3", roundDf.format(1.25))
        roundDf.setRoundingMode(RoundingMode.DOWN)
        assertEquals("1.2", roundDf.format(1.29))

        // High precision Long.MAX_VALUE and BigDecimal formatting
        val intDf = DecimalFormat("#0", DecimalFormatSymbols.getInstance(Locale.US))
        assertEquals(Long.MAX_VALUE.toString(), intDf.format(Long.MAX_VALUE))
        assertEquals(Long.MAX_VALUE, intDf.parse(Long.MAX_VALUE.toString()).toLong())
        assertEquals("1,234.56", usDf.format(BigDecimal("1234.56")))

        // Invalid pattern throws IllegalArgumentException
        assertFailsWith<IllegalArgumentException> {
            DecimalFormat("0.0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            DecimalFormat("0#0.0")
        }
    }

    @Test
    fun testFormatAndFieldPosition() {
        val df = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
        val genericFormat: Format = df
        assertEquals("1,234.56", genericFormat.format(1234.56))
        assertEquals(1234.56, (genericFormat.parseObject("1,234.56") as Number).toDouble(), 1e-6)

        val errPos = ParsePosition(0)
        assertNull(genericFormat.parseObject("not-a-number", errPos))
        assertEquals(0, errPos.errorIndex)
        assertFailsWith<ParseException> {
            genericFormat.parseObject("not-a-number")
        }

        // FieldPosition contract
        val intFp = FieldPosition(NumberFormat.INTEGER_FIELD)
        assertEquals(NumberFormat.INTEGER_FIELD, intFp.field)
        assertEquals(NumberFormat.INTEGER_FIELD, intFp.getField())
        intFp.setBeginIndex(0)
        intFp.setEndIndex(5)
        assertEquals(0, intFp.beginIndex)
        assertEquals(5, intFp.endIndex)
        assertEquals(0, intFp.getBeginIndex())
        assertEquals(5, intFp.getEndIndex())

        val fracFp = FieldPosition(NumberFormat.FRACTION_FIELD)
        fracFp.beginIndex = 6
        fracFp.endIndex = 8
        assertEquals(6, fracFp.getBeginIndex())
        assertEquals(8, fracFp.getEndIndex())

        val fpCopy = FieldPosition(NumberFormat.FRACTION_FIELD)
        fpCopy.beginIndex = fracFp.beginIndex
        fpCopy.endIndex = fracFp.endIndex
        assertEquals(fracFp, fpCopy)
        assertEquals(fracFp.hashCode(), fpCopy.hashCode())
        assertNotEquals(intFp, fracFp)
        assertTrue(fracFp.toString().contains("beginIndex=6"))
    }

    @Test
    fun testDateFormatAndSimpleDateFormatLocalesAndStrictness() {
        val utc = TimeZone.getTimeZone("UTC")
        val epoch = Date(0L) // 1970-01-01T00:00:00Z

        val dfShort: DateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US)
        dfShort.timeZone = utc
        val formattedShort = dfShort.format(epoch)
        assertTrue(formattedShort.isNotEmpty())
        assertEquals(epoch, dfShort.parse(formattedShort))

        val dtMedium: DateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US)
        dtMedium.timeZone = utc
        val formattedMed = dtMedium.format(epoch)
        assertEquals(epoch, dtMedium.parse(formattedMed))

        // DateFormat field constants with FieldPosition
        val yearFp = FieldPosition(DateFormat.YEAR_FIELD)
        val monthFp = FieldPosition(DateFormat.MONTH_FIELD)
        val dateFp = FieldPosition(DateFormat.DATE_FIELD)
        assertEquals(DateFormat.YEAR_FIELD, yearFp.field)
        assertEquals(DateFormat.MONTH_FIELD, monthFp.field)
        assertEquals(DateFormat.DATE_FIELD, dateFp.field)
        assertNotEquals(yearFp, monthFp)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = utc
        assertEquals("1970-01-01", sdf.format(epoch))

        // Multi-locale month/weekday formatting & parsing
        for (locale in listOf(Locale.US, Locale.CHINA, Locale.GERMANY, Locale.FRANCE)) {
            val locSdf = SimpleDateFormat("yyyy MMM dd EEEE a", locale)
            locSdf.timeZone = utc
            val str = locSdf.format(epoch)
            val parsed = locSdf.parse(str)
            assertNotNull(parsed)
            assertEquals(0L, parsed.time, "Failed round-trip for locale $locale: $str")
        }

        // Adjacent numeric parsing (yyyyMMddHHmmss)
        val compactSdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        compactSdf.timeZone = utc
        assertEquals("19700101000000", compactSdf.format(epoch))
        assertEquals(0L, compactSdf.parse("19700101000000").time)

        // Strict vs lenient parsing
        val strictSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        strictSdf.timeZone = utc
        strictSdf.isLenient = false
        assertEquals(false, strictSdf.isLenient)
        assertFailsWith<ParseException> {
            strictSdf.parse("2024-02-30")
        }
        assertFailsWith<ParseException> {
            strictSdf.parse("2024-13-01")
        }
        // Leap day 2024-02-29 is valid; 2023-02-29 is invalid
        assertNotNull(strictSdf.parse("2024-02-29"))
        assertFailsWith<ParseException> {
            strictSdf.parse("2023-02-29")
        }
    }

    @Test
    fun testScientificNotationAndAdditionalLocales() {
        val usSymbols = DecimalFormatSymbols.getInstance(Locale.US)
        val sci1 = DecimalFormat("0.###E0", usSymbols)
        assertEquals("1.235E4", sci1.format(12345.67))
        assertEquals("1.23E-4", sci1.format(0.000123))
        assertEquals("0E0", sci1.format(0.0))
        assertEquals("-1.235E4", sci1.format(-12345.67))
        assertEquals(12345.0, sci1.parse("1.2345E4").toDouble(), 1e-6)
        assertEquals(0.00125, sci1.parse("1.25E-3").toDouble(), 1e-9)

        val sciFixed = DecimalFormat("0.00E00", usSymbols)
        assertEquals("1.23E04", sciFixed.format(12345.0))
        assertEquals("1.23E-04", sciFixed.format(0.000123))

        val eng = DecimalFormat("##0.##E0", usSymbols)
        assertEquals("12.345E3", eng.format(12345.0))

        // Verify Locale.ITALY and Locale.KOREA symbols in SimpleDateFormat
        val utc = TimeZone.getTimeZone("UTC")
        val epoch = Date(0L) // 1970-01-01 Thursday
        for (locale in listOf(Locale.ITALY, Locale.KOREA)) {
            val sdf = SimpleDateFormat("yyyy MMMM dd EEEE a HH:mm", locale)
            sdf.timeZone = utc
            val text = sdf.format(epoch)
            assertEquals(0L, sdf.parse(text).time, "Round-trip failed for $locale: $text")
        }
        val itSdf = SimpleDateFormat("MMMM", Locale.ITALY).apply { timeZone = utc }
        assertEquals("gennaio", itSdf.format(epoch).lowercase())
        val koSdf = SimpleDateFormat("MMMM", Locale.KOREA).apply { timeZone = utc }
        assertEquals("1월", koSdf.format(epoch))
    }
}


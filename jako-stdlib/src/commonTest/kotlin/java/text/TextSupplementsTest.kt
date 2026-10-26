package jako.test.text

import java.text.*
import java.text.CollationKey
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TextSupplementsTest {

    @Test
    fun testDecimalFormatSymbols() {
        val symbols = DecimalFormatSymbols(Locale.US)
        assertEquals('.', symbols.getDecimalSeparator())
        assertEquals(',', symbols.getGroupingSeparator())
        assertEquals('0', symbols.getZeroDigit())
        assertEquals('%', symbols.getPercent())
        assertEquals('-', symbols.getMinusSign())

        val custom = symbols.clone() as DecimalFormatSymbols
        assertEquals(symbols, custom)
        custom.setDecimalSeparator(',')
        custom.setGroupingSeparator('_')
        assertNotEquals(symbols, custom)

        val df = DecimalFormat("#,##0.00", custom)
        assertEquals("1_234,50", df.format(1234.5))
        assertEquals(1234.5, df.parse("1_234,50")!!.toDouble(), 0.001)

        // Defensive copy check
        val retrieved = df.getDecimalFormatSymbols()
        assertEquals(',', retrieved.getDecimalSeparator())
        assertEquals('_', retrieved.getGroupingSeparator())
    }

    @Test
    fun testDateFormatSymbols() {
        val dfs = DateFormatSymbols.getInstance(Locale.US)
        assertTrue(dfs.getMonths().size >= 12)
        assertEquals("January", dfs.getMonths()[0])
        assertEquals("Jan", dfs.getShortMonths()[0])
        assertEquals("Sunday", dfs.getWeekdays()[1])
        assertEquals("Sun", dfs.getShortWeekdays()[1])
        assertContentEquals(arrayOf("AM", "PM"), dfs.getAmPmStrings())

        val custom = dfs.clone() as DateFormatSymbols
        assertEquals(dfs, custom)
        val customMonths = custom.getShortMonths()
        customMonths[0] = "JAN_CUSTOM"
        custom.setShortMonths(customMonths)

        val sdf = SimpleDateFormat("dd-MMM-yyyy", custom)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        assertEquals("dd-MMM-yyyy", sdf.toPattern())
        val formatted = sdf.format(Date(0L))
        assertEquals("01-JAN_CUSTOM-1970", formatted)
        val parsed = sdf.parse("01-JAN_CUSTOM-1970")
        assertEquals(0L, parsed.time)
    }

    @Test
    fun testCharacterIteratorAndStringCharacterIterator() {
        val iter = StringCharacterIterator("Hello", 1, 4, 2) // "ell", pos at 'l' (index 2)
        assertEquals(1, iter.getBeginIndex())
        assertEquals(4, iter.getEndIndex())
        assertEquals(2, iter.getIndex())
        assertEquals('l', iter.current())

        assertEquals('e', iter.first())
        assertEquals(1, iter.getIndex())
        assertEquals('l', iter.next())
        assertEquals('l', iter.next())
        assertEquals(CharacterIterator.DONE, iter.next())
        assertEquals(4, iter.getIndex())

        assertEquals('l', iter.last())
        assertEquals(3, iter.getIndex())
        assertEquals('l', iter.previous())
        assertEquals('e', iter.previous())
        assertEquals(CharacterIterator.DONE, iter.previous())

        val cloned = iter.clone() as StringCharacterIterator
        assertEquals(iter, cloned)

        assertFailsWith<IllegalArgumentException> {
            iter.setIndex(0)
        }
    }

    @Test
    fun testCollatorAndCollationKey() {
        val collator = Collator.getInstance(Locale.US)
        collator.setStrength(Collator.PRIMARY)
        assertEquals(Collator.PRIMARY, collator.getStrength())
        assertEquals(0, collator.compare("abc", "ABC"))
        assertEquals(0, collator.compare("cafe", "caf\u00e9"))
        assertTrue(collator.equals("abc", "ABC"))

        collator.setStrength(Collator.SECONDARY)
        assertEquals(0, collator.compare("abc", "ABC"))
        assertTrue(collator.compare("cafe", "caf\u00e9") < 0)

        collator.setStrength(Collator.TERTIARY)
        assertTrue(collator.compare("abc", "ABC") < 0)
        assertTrue(collator.compare("A", "b") < 0)

        // Canonical decomposition vs no decomposition on precomposed/decomposed "café"
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION)
        assertEquals(Collator.CANONICAL_DECOMPOSITION, collator.getDecomposition())
        assertEquals(0, collator.compare("caf\u00e9", "cafe\u0301"))

        val k1: CollationKey = collator.getCollationKey("apple")
        val k2: CollationKey = collator.getCollationKey("banana")
        val k3: CollationKey = collator.getCollationKey("apple")
        assertEquals("apple", k1.sourceString)
        assertEquals("apple", k1.getSourceString())
        assertTrue(k1.compareTo(k2) < 0)
        assertTrue(k2.compareTo(k1) > 0)
        assertEquals(0, k1.compareTo(k3))
        assertEquals(k1, k3)
        assertEquals(k1.hashCode(), k3.hashCode())
        assertNotEquals(k1, k2)
        assertTrue(k1.toByteArray().isNotEmpty())
        assertContentEquals(k1.toByteArray(), k3.toByteArray())
    }

    @Test
    fun testBreakIterator() {
        // 1. Character boundaries
        val charIter = BreakIterator.getCharacterInstance(Locale.US)
        charIter.setText("A\u0301B") // 'A' + combining acute accent + 'B'
        assertEquals(0, charIter.first())
        assertEquals(2, charIter.next())
        assertEquals(3, charIter.next())
        assertEquals(BreakIterator.DONE, charIter.next())

        // 2. Word boundaries
        val wordIter = BreakIterator.getWordInstance(Locale.US)
        wordIter.setText("Hello, world!")
        val boundaries = mutableListOf<Int>()
        var pos = wordIter.first()
        while (pos != BreakIterator.DONE) {
            boundaries.add(pos)
            pos = wordIter.next()
        }
        assertEquals(listOf(0, 5, 6, 7, 12, 13), boundaries)
        assertTrue(wordIter.isBoundary(5))
        assertFalse(wordIter.isBoundary(2))
        assertEquals(5, wordIter.following(2))
        assertEquals(0, wordIter.preceding(2))

        // 3. Sentence boundaries
        val sentIter = BreakIterator.getSentenceInstance(Locale.US)
        sentIter.setText("Hello world. How are you?")
        assertEquals(0, sentIter.first())
        assertEquals(13, sentIter.next())
        assertEquals(25, sentIter.next())
        assertEquals(BreakIterator.DONE, sentIter.next())
    }

    @Test
    fun testChoiceFormat() {
        val next = ChoiceFormat.nextDouble(1.0)
        val prev = ChoiceFormat.previousDouble(next)
        assertTrue(next > 1.0)
        assertEquals(1.0, prev)

        val cf = ChoiceFormat("-1#is negative|0#is zero or fraction|1#is one|1.0<is 1+|2#is two|2<is more than 2.")
        assertEquals("is negative", cf.format(-5))
        assertEquals("is zero or fraction", cf.format(0))
        assertEquals("is zero or fraction", cf.format(0.5))
        assertEquals("is one", cf.format(1.0))
        assertEquals("is 1+", cf.format(1.5))
        assertEquals("is two", cf.format(2.0))
        assertEquals("is more than 2.", cf.format(3.0))

        val pp = ParsePosition(0)
        assertEquals(2.0, cf.parse("is two", pp)!!.toDouble())
        assertEquals(6, pp.getIndex())
    }

    @Test
    fun testBidi() {
        val ltr = Bidi("Hello World", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)
        assertTrue(ltr.isLeftToRight())
        assertFalse(ltr.isRightToLeft())
        assertFalse(ltr.isMixed())
        assertTrue(ltr.baseIsLeftToRight())
        assertEquals(0, ltr.getBaseLevel())
        assertEquals(1, ltr.getRunCount())
        assertEquals(0, ltr.getRunStart(0))
        assertEquals(11, ltr.getRunLimit(0))
        assertEquals(0, ltr.getRunLevel(0))

        val rtlText = "\u05D0\u05D1\u05D2" // Hebrew Alef Bet Gimel
        assertTrue(Bidi.requiresBidi(rtlText.toCharArray(), 0, rtlText.length))
        assertFalse(Bidi.requiresBidi("ABC".toCharArray(), 0, 3))

        val rtl = Bidi(rtlText, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)
        assertTrue(rtl.isRightToLeft())
        assertFalse(rtl.isLeftToRight())
        assertFalse(rtl.baseIsLeftToRight())
        assertEquals(1, rtl.getBaseLevel())

        val mixed = Bidi("Hello \u05D0\u05D1", Bidi.DIRECTION_LEFT_TO_RIGHT)
        assertTrue(mixed.isMixed())
        assertEquals(2, mixed.getRunCount())
        assertEquals(0, mixed.getRunLevel(0))
        assertEquals(1, mixed.getRunLevel(1))

        val lineBidi = mixed.createLineBidi(0, 5)
        assertTrue(lineBidi.isLeftToRight())
        assertEquals(5, lineBidi.getLength())

        val objs: Array<Any?> = arrayOf("A", "B", "C", "D")
        val levels = byteArrayOf(0, 1, 1, 0)
        Bidi.reorderVisually(levels, 0, objs, 0, 4)
        assertContentEquals(arrayOf<Any?>("A", "C", "B", "D"), objs)
    }
}

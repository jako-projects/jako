package jako.test.util

import java.util.AbstractSequentialList
import java.util.Dictionary
import java.util.DoubleSummaryStatistics
import java.util.DuplicateFormatFlagsException
import java.util.EmptyStackException
import java.util.EventListener
import java.util.EventListenerProxy
import java.util.Formattable
import java.util.FormattableFlags
import java.util.Formatter
import java.util.FormatterClosedException
import java.util.Hashtable
import java.util.IllformedLocaleException
import java.util.IntSummaryStatistics
import java.util.LinkedList
import java.util.LongSummaryStatistics
import java.util.MissingFormatArgumentException
import java.util.MissingResourceException
import java.util.Properties
import java.util.Spliterator
import java.util.Spliterators
import java.util.Stack
import java.util.TooManyListenersException
import java.util.UnknownFormatConversionException
import java.util.Vector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LegacyCollectionsAndSummaryStatsTest {

    @Test
    fun testHashtableAndPropertiesHierarchy() {
        val table = Hashtable<String, Int>()
        assertTrue(table.isEmpty())
        table["alpha"] = 1
        table["beta"] = 2
        assertEquals(2, table.size)
        assertTrue(table.contains(2))
        assertTrue(table.containsKey("alpha"))
        assertEquals(1, table["alpha"])

        val dict: Dictionary<String, Int> = table
        assertEquals(2, dict["beta"])

        val props = Properties()
        props.setProperty("k", "v")
        val asTable: Hashtable<Any, Any> = props
        assertEquals("v", asTable["k"])
    }

    @Test
    fun testVectorAndStack() {
        val vec = Vector<String>(2, 5)
        vec.addElement("a")
        vec.addElement("b")
        vec.insertElementAt("c", 1)
        assertEquals(listOf("a", "c", "b"), vec)
        assertEquals("a", vec.firstElement())
        assertEquals("b", vec.lastElement())

        val stack = Stack<Int>()
        assertTrue(stack.empty())
        assertFailsWith<EmptyStackException> { stack.peek() }
        stack.push(10)
        stack.push(20)
        stack.push(30)
        assertEquals(1, stack.search(30))
        assertEquals(3, stack.search(10))
        assertEquals(-1, stack.search(99))
        assertEquals(30, stack.peek())
        assertEquals(30, stack.pop())
        assertEquals(2, stack.size)
    }

    @Test
    fun testLinkedListAndAbstractSequentialList() {
        val list = LinkedList<String>()
        list.addFirst("b")
        list.addFirst("a")
        list.addLast("c")
        val seqList: AbstractSequentialList<String> = list
        assertEquals(listOf("a", "b", "c"), seqList)
        assertEquals("a", list.getFirst())
        assertEquals("c", list.getLast())
        assertEquals("c", list.removeLast())
        assertEquals(2, list.size)

        val desc = mutableListOf<String>()
        val iter = list.descendingIterator()
        while (iter.hasNext()) desc.add(iter.next())
        assertEquals(listOf("b", "a"), desc)
    }

    @Test
    fun testSummaryStatistics() {
        val intStats = IntSummaryStatistics()
        intStats.accept(10)
        intStats.accept(20)
        intStats.accept(30)
        assertEquals(3L, intStats.count)
        assertEquals(60L, intStats.sum)
        assertEquals(10, intStats.min)
        assertEquals(30, intStats.max)
        assertEquals(20.0, intStats.average)

        val longStats = LongSummaryStatistics()
        longStats.accept(5)
        longStats.accept(15L)
        assertEquals(2L, longStats.count)
        assertEquals(20L, longStats.sum)
        assertEquals(5L, longStats.min)
        assertEquals(15L, longStats.max)

        val dblStats = DoubleSummaryStatistics()
        dblStats.accept(1.5)
        dblStats.accept(2.5)
        assertEquals(2L, dblStats.count)
        assertEquals(4.0, dblStats.sum)
        assertEquals(1.5, dblStats.min)
        assertEquals(2.5, dblStats.max)
        assertEquals(2.0, dblStats.average)
    }

    private interface CustomListener : EventListener
    private class CustomListenerProxy(l: CustomListener) : EventListenerProxy<CustomListener>(l)

    @Test
    fun testFormattableAndExceptions() {
        val custom = Formattable { formatter, flags, _, _ ->
            val text = if ((flags and FormattableFlags.UPPERCASE) != 0) "CUSTOM" else "custom"
            formatter.format("%s", text)
        }
        assertEquals("val=custom", Formatter().format("val=%s", custom).toString())
        assertEquals("val=CUSTOM", Formatter().format("val=%S", custom).toString())

        val f = Formatter()
        f.close()
        assertFailsWith<FormatterClosedException> { f.toString() }
        assertFailsWith<MissingFormatArgumentException> { Formatter().format("%s %s", "onlyOne") }
        assertFailsWith<UnknownFormatConversionException> { Formatter().format("%z", "arg") }

        val dup = DuplicateFormatFlagsException("-")
        assertEquals("-", dup.flags)

        val ill = IllformedLocaleException("Bad subtag", 3)
        assertEquals(3, ill.errorIndex)

        val mre = MissingResourceException("Not found", "MyBundle", "key1")
        assertEquals("MyBundle", mre.className)
        assertEquals("key1", mre.key)

        val tmle = TooManyListenersException("Only one allowed")
        assertEquals("Only one allowed", tmle.message)

        val listener = object : CustomListener {}
        val proxy = CustomListenerProxy(listener)
        assertEquals(listener, proxy.listener)
    }

    @Test
    fun testSpliterators() {
        val split = Spliterators.spliterator(listOf(1, 2, 3, 4), Spliterator.ORDERED)
        assertEquals(4L, split.estimateSize())
        assertTrue(split.hasCharacteristics(Spliterator.ORDERED))
        val firstHalf = split.trySplit()
        val collected = mutableListOf<Int>()
        firstHalf?.forEachRemaining { collected.add(it) }
        split.forEachRemaining { collected.add(it) }
        assertEquals(listOf(1, 2, 3, 4), collected)
    }
}

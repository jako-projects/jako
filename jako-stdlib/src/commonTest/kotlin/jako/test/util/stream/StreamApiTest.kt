package jako.test.util.stream

import java.lang.Runnable
import java.util.Arrays
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.IntPredicate
import java.util.function.IntUnaryOperator
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.function.ToIntFunction
import java.util.function.UnaryOperator
import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.streams.asSequence
import kotlin.streams.asStream
import kotlin.streams.toList
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StreamApiTest {

    @Test
    fun testStreamOperationsAndCollectors() {
        val words = Stream.of("apple", "banana", "apricot", "blueberry", "avocado", "banana")
            .filter(Predicate { it.startsWith("a") })
            .distinct()
            .sorted()
            .map(Function { it.uppercase() })
            .collect(Collectors.toList())

        assertEquals(listOf("APPLE", "APRICOT", "AVOCADO"), words)

        val joined = Stream.of("x", "y", "z")
            .collect(Collectors.joining(", ", "[", "]"))
        assertEquals("[x, y, z]", joined)

        val grouped = Stream.of("one", "two", "three", "four", "five")
            .collect(Collectors.groupingBy<String, Int>(Function { it.length }))
        assertEquals<List<String>?>(listOf("one", "two"), grouped[3])
        assertEquals<List<String>?>(listOf("four", "five"), grouped[4])
        assertEquals<List<String>?>(listOf("three"), grouped[5])

        val partitioned = Stream.of(1, 2, 3, 4, 5, 6)
            .collect(Collectors.partitioningBy<Int>(Predicate { it % 2 == 0 }))
        assertEquals<List<Int>?>(listOf(2, 4, 6), partitioned[true])
        assertEquals<List<Int>?>(listOf(1, 3, 5), partitioned[false])

        val map = Stream.of("a:1", "b:2", "a:3")
            .collect(
                Collectors.toMap(
                    Function { it.substringBefore(":") },
                    Function { it.substringAfter(":").toInt() },
                    BinaryOperator { v1, v2 -> v1 + v2 }
                )
            )
        assertEquals(4, map["a"])
        assertEquals(2, map["b"])

        val stats = Stream.of("hi", "hello", "hey")
            .collect(Collectors.summarizingInt(ToIntFunction { it.length }))
        assertEquals(3L, stats.count)
        assertEquals(10L, stats.sum)
        assertEquals(2, stats.min)
        assertEquals(5, stats.max)
    }

    @Test
    fun testStreamBuildersIterateConcatAndClose() {
        val streamBuilder: Stream.Builder<String> = Stream.builder()
        streamBuilder.accept("zero")
        val built = streamBuilder
            .add("first")
            .add("second")
            .build()
            .collect(Collectors.toList())
        assertEquals(listOf("zero", "first", "second"), built)
        kotlin.test.assertFailsWith<IllegalStateException> {
            streamBuilder.add("after-build")
        }

        val intBuilder: IntStream.Builder = IntStream.builder()
        intBuilder.accept(10)
        intBuilder.add(20).add(30)
        assertContentEquals(intArrayOf(10, 20, 30), intBuilder.build().toArray())
        kotlin.test.assertFailsWith<IllegalStateException> {
            intBuilder.add(40)
        }

        val longBuilder: LongStream.Builder = LongStream.builder()
        longBuilder.accept(100L)
        longBuilder.add(200L)
        assertContentEquals(longArrayOf(100L, 200L), longBuilder.build().toArray())
        kotlin.test.assertFailsWith<IllegalStateException> {
            longBuilder.add(300L)
        }

        val doubleBuilder: DoubleStream.Builder = DoubleStream.builder()
        doubleBuilder.accept(1.5)
        doubleBuilder.add(2.5)
        assertContentEquals(doubleArrayOf(1.5, 2.5), doubleBuilder.build().toArray())
        kotlin.test.assertFailsWith<IllegalStateException> {
            doubleBuilder.add(3.5)
        }

        val powersOfTwo = Stream.iterate(1, UnaryOperator { it * 2 })
            .limit(5)
            .collect(Collectors.toList())
        assertEquals(listOf(1, 2, 4, 8, 16), powersOfTwo)

        var closedCount = 0
        val s1 = Stream.of(1, 2).onClose(Runnable { closedCount++ })
        val s2 = Stream.of(3, 4).onClose(Runnable { closedCount += 10 })
        val combined = Stream.concat(s1, s2)
        assertEquals(10, combined.reduce(0, BinaryOperator { a, b -> a + b }))
        combined.close()
        assertEquals(11, closedCount)
    }

    @Test
    fun testPrimitiveStreamsAndArraysStream() {
        val sumOfSquares = IntStream.rangeClosed(1, 5)
            .filter(IntPredicate { it % 2 == 1 })
            .map(IntUnaryOperator { it * it })
            .sum()
        // 1^2 + 3^2 + 5^2 = 1 + 9 + 25 = 35
        assertEquals(35, sumOfSquares)

        val avgOpt = IntStream.range(1, 5).average()
        assertTrue(avgOpt.isPresent())
        assertEquals(2.5, avgOpt.getAsDouble())

        val longArr = LongStream.rangeClosed(10L, 12L).toArray()
        assertContentEquals(longArrayOf(10L, 11L, 12L), longArr)

        val dblSum = DoubleStream.of(1.5, 2.5, 3.0).sum()
        assertEquals(7.0, dblSum)

        val fromIntArray = Arrays.stream(intArrayOf(10, 20, 30, 40), 1, 3).toArray()
        assertContentEquals(intArrayOf(20, 30), fromIntArray)

        val fromObjArray = Arrays.stream(arrayOf("a", "b", "c"))
            .anyMatch(Predicate { it == "b" })
        assertTrue(fromObjArray)
        assertFalse(IntStream.of(2, 4, 6).anyMatch(IntPredicate { it % 2 != 0 }))

        val customCollector = Collector.of(
            Supplier { ArrayList<Int>() },
            { list, v: Int -> list.add(v * 10) },
            BinaryOperator { l1, l2 -> l1.addAll(l2); l1 },
            Collector.Characteristics.IDENTITY_FINISH,
            Collector.Characteristics.UNORDERED
        )
        assertTrue(customCollector.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH))
        assertTrue(customCollector.characteristics().contains(Collector.Characteristics.UNORDERED))
        assertFalse(customCollector.characteristics().contains(Collector.Characteristics.CONCURRENT))
        val collected = StreamSupport.stream(Arrays.spliterator(arrayOf(1, 2, 3)), false)
            .collect(customCollector)
        assertEquals(listOf(10, 20, 30), collected)
    }

    @Test
    fun testKotlinStreamsExtensions() {
        val seq = Stream.of("x", "y", "z").asSequence()
        assertEquals(listOf("x", "y", "z"), seq.toList())

        val intSeq = IntStream.rangeClosed(1, 3).asSequence()
        assertEquals(listOf(1, 2, 3), intSeq.toList())

        val longList = LongStream.of(100L, 200L).toList()
        assertEquals(listOf(100L, 200L), longList)

        val dblList = DoubleStream.of(1.25, 2.5).toList()
        assertEquals(listOf(1.25, 2.5), dblList)

        val fromSequence = sequenceOf(2, 4, 6).asStream().toList()
        assertEquals(listOf(2, 4, 6), fromSequence)
    }
}

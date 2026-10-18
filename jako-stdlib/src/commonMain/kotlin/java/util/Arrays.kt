@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect class Arrays {
    companion object {
        fun <T> asList(vararg a: T): List<T>
        fun <T> sort(a: Array<T>)
        fun <T> sort(a: Array<T>, c: Comparator<in T>?)
        fun <T> sort(a: Array<T>, fromIndex: Int, toIndex: Int)
        fun <T> sort(a: Array<T>, fromIndex: Int, toIndex: Int, c: Comparator<in T>?)
        fun sort(a: ByteArray)
        fun sort(a: ByteArray, fromIndex: Int, toIndex: Int)
        fun sort(a: CharArray)
        fun sort(a: CharArray, fromIndex: Int, toIndex: Int)
        fun sort(a: ShortArray)
        fun sort(a: ShortArray, fromIndex: Int, toIndex: Int)
        fun sort(a: IntArray)
        fun sort(a: IntArray, fromIndex: Int, toIndex: Int)
        fun sort(a: LongArray)
        fun sort(a: LongArray, fromIndex: Int, toIndex: Int)
        fun sort(a: FloatArray)
        fun sort(a: FloatArray, fromIndex: Int, toIndex: Int)
        fun sort(a: DoubleArray)
        fun sort(a: DoubleArray, fromIndex: Int, toIndex: Int)

        fun binarySearch(a: ByteArray, key: Byte): Int
        fun binarySearch(a: CharArray, key: Char): Int
        fun binarySearch(a: ShortArray, key: Short): Int
        fun binarySearch(a: IntArray, key: Int): Int
        fun binarySearch(a: LongArray, key: Long): Int
        fun binarySearch(a: FloatArray, key: Float): Int
        fun binarySearch(a: DoubleArray, key: Double): Int
        fun binarySearch(a: Array<out Any>, key: Any): Int
        fun <T> binarySearch(a: Array<out T>, key: T, c: Comparator<in T>?): Int

        fun equals(a: Array<*>?, b: Array<*>?): Boolean
        fun equals(a: ByteArray?, b: ByteArray?): Boolean
        fun equals(a: CharArray?, b: CharArray?): Boolean
        fun equals(a: ShortArray?, b: ShortArray?): Boolean
        fun equals(a: IntArray?, b: IntArray?): Boolean
        fun equals(a: LongArray?, b: LongArray?): Boolean
        fun equals(a: FloatArray?, b: FloatArray?): Boolean
        fun equals(a: DoubleArray?, b: DoubleArray?): Boolean
        fun equals(a: BooleanArray?, b: BooleanArray?): Boolean

        fun fill(a: Array<Any?>, value: Any?)
        fun fill(a: Array<Any?>, fromIndex: Int, toIndex: Int, value: Any?)
        fun fill(a: ByteArray, value: Byte)
        fun fill(a: ByteArray, fromIndex: Int, toIndex: Int, value: Byte)
        fun fill(a: CharArray, value: Char)
        fun fill(a: CharArray, fromIndex: Int, toIndex: Int, value: Char)
        fun fill(a: ShortArray, value: Short)
        fun fill(a: ShortArray, fromIndex: Int, toIndex: Int, value: Short)
        fun fill(a: IntArray, value: Int)
        fun fill(a: IntArray, fromIndex: Int, toIndex: Int, value: Int)
        fun fill(a: LongArray, value: Long)
        fun fill(a: LongArray, fromIndex: Int, toIndex: Int, value: Long)
        fun fill(a: FloatArray, value: Float)
        fun fill(a: FloatArray, fromIndex: Int, toIndex: Int, value: Float)
        fun fill(a: DoubleArray, value: Double)
        fun fill(a: DoubleArray, fromIndex: Int, toIndex: Int, value: Double)
        fun fill(a: BooleanArray, value: Boolean)
        fun fill(a: BooleanArray, fromIndex: Int, toIndex: Int, value: Boolean)

        fun copyOf(original: ByteArray, newLength: Int): ByteArray
        fun copyOf(original: CharArray, newLength: Int): CharArray
        fun copyOf(original: ShortArray, newLength: Int): ShortArray
        fun copyOf(original: IntArray, newLength: Int): IntArray
        fun copyOf(original: LongArray, newLength: Int): LongArray
        fun copyOf(original: FloatArray, newLength: Int): FloatArray
        fun copyOf(original: DoubleArray, newLength: Int): DoubleArray
        fun copyOf(original: BooleanArray, newLength: Int): BooleanArray

        fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray
        fun copyOfRange(original: CharArray, from: Int, to: Int): CharArray
        fun copyOfRange(original: ShortArray, from: Int, to: Int): ShortArray
        fun copyOfRange(original: IntArray, from: Int, to: Int): IntArray
        fun copyOfRange(original: LongArray, from: Int, to: Int): LongArray
        fun copyOfRange(original: FloatArray, from: Int, to: Int): FloatArray
        fun copyOfRange(original: DoubleArray, from: Int, to: Int): DoubleArray
        fun copyOfRange(original: BooleanArray, from: Int, to: Int): BooleanArray

        fun hashCode(a: Array<*>?): Int
        fun hashCode(a: ByteArray?): Int
        fun hashCode(a: CharArray?): Int
        fun hashCode(a: ShortArray?): Int
        fun hashCode(a: IntArray?): Int
        fun hashCode(a: LongArray?): Int
        fun hashCode(a: FloatArray?): Int
        fun hashCode(a: DoubleArray?): Int
        fun hashCode(a: BooleanArray?): Int

        fun toString(a: Array<*>?): String
        fun toString(a: ByteArray?): String
        fun toString(a: CharArray?): String
        fun toString(a: ShortArray?): String
        fun toString(a: IntArray?): String
        fun toString(a: LongArray?): String
        fun toString(a: FloatArray?): String
        fun toString(a: DoubleArray?): String
        fun toString(a: BooleanArray?): String

        fun deepToString(a: Array<*>?): String
        fun deepEquals(a1: Array<*>?, a2: Array<*>?): Boolean
        fun deepHashCode(a: Array<*>?): Int

        fun <T> spliterator(array: Array<T>): Spliterator<T>
        fun <T> spliterator(array: Array<T>, startInclusive: Int, endExclusive: Int): Spliterator<T>
        fun spliterator(array: IntArray): Spliterator.OfInt
        fun spliterator(array: IntArray, startInclusive: Int, endExclusive: Int): Spliterator.OfInt
        fun spliterator(array: LongArray): Spliterator.OfLong
        fun spliterator(array: LongArray, startInclusive: Int, endExclusive: Int): Spliterator.OfLong
        fun spliterator(array: DoubleArray): Spliterator.OfDouble
        fun spliterator(array: DoubleArray, startInclusive: Int, endExclusive: Int): Spliterator.OfDouble

        fun <T> stream(array: Array<T>): java.util.stream.Stream<T>
        fun <T> stream(array: Array<T>, startInclusive: Int, endExclusive: Int): java.util.stream.Stream<T>
        fun stream(array: IntArray): java.util.stream.IntStream
        fun stream(array: IntArray, startInclusive: Int, endExclusive: Int): java.util.stream.IntStream
        fun stream(array: LongArray): java.util.stream.LongStream
        fun stream(array: LongArray, startInclusive: Int, endExclusive: Int): java.util.stream.LongStream
        fun stream(array: DoubleArray): java.util.stream.DoubleStream
        fun stream(array: DoubleArray, startInclusive: Int, endExclusive: Int): java.util.stream.DoubleStream
    }
}


package java.util

actual class Arrays {
    actual companion object {
        actual fun <T> asList(vararg a: T): List<T> = a.asList()

        actual fun <T> sort(a: Array<T>) {
            (a as Array<Comparable<Any>>).sort()
        }

        actual fun <T> sort(a: Array<T>, c: Comparator<in T>?) {
            if (c != null) a.sortWith(c) else sort(a)
        }

        actual fun <T> sort(a: Array<T>, fromIndex: Int, toIndex: Int) {
            (a as Array<Comparable<Any>>).sort(fromIndex, toIndex)
        }

        actual fun <T> sort(a: Array<T>, fromIndex: Int, toIndex: Int, c: Comparator<in T>?) {
            if (c != null) a.sortWith(c, fromIndex, toIndex) else sort(a, fromIndex, toIndex)
        }

        actual fun sort(a: ByteArray) = a.sort()
        actual fun sort(a: ByteArray, fromIndex: Int, toIndex: Int) = a.sort(fromIndex, toIndex)
        actual fun sort(a: CharArray) = a.sort()
        actual fun sort(a: CharArray, fromIndex: Int, toIndex: Int) = a.sort(fromIndex, toIndex)
        actual fun sort(a: ShortArray) = a.sort()
        actual fun sort(a: ShortArray, fromIndex: Int, toIndex: Int) = a.sort(fromIndex, toIndex)
        actual fun sort(a: IntArray) = a.sort()
        actual fun sort(a: IntArray, fromIndex: Int, toIndex: Int) = a.sort(fromIndex, toIndex)
        actual fun sort(a: LongArray) = a.sort()
        actual fun sort(a: LongArray, fromIndex: Int, toIndex: Int) = a.sort(fromIndex, toIndex)
        actual fun sort(a: FloatArray) = a.sort()
        actual fun sort(a: FloatArray, fromIndex: Int, toIndex: Int) = a.sort(fromIndex, toIndex)
        actual fun sort(a: DoubleArray) = a.sort()
        actual fun sort(a: DoubleArray, fromIndex: Int, toIndex: Int) = a.sort(fromIndex, toIndex)

        actual fun binarySearch(a: ByteArray, key: Byte): Int = a.asList().binarySearch(key)
        actual fun binarySearch(a: CharArray, key: Char): Int = a.asList().binarySearch(key)
        actual fun binarySearch(a: ShortArray, key: Short): Int = a.asList().binarySearch(key)
        actual fun binarySearch(a: IntArray, key: Int): Int = a.asList().binarySearch(key)
        actual fun binarySearch(a: LongArray, key: Long): Int = a.asList().binarySearch(key)
        actual fun binarySearch(a: FloatArray, key: Float): Int = a.asList().binarySearch(key)
        actual fun binarySearch(a: DoubleArray, key: Double): Int = a.asList().binarySearch(key)
        actual fun binarySearch(a: Array<out Any>, key: Any): Int = (a.asList() as List<Comparable<Any>>).binarySearch(key as Comparable<Any>)
        actual fun <T> binarySearch(a: Array<out T>, key: T, c: Comparator<in T>?): Int {
            return if (c != null) a.asList().binarySearch(key, c) else binarySearch(a as Array<out Any>, key as Any)
        }

        actual fun equals(a: Array<*>?, b: Array<*>?): Boolean = a.contentEquals(b)
        actual fun equals(a: ByteArray?, b: ByteArray?): Boolean = a.contentEquals(b)
        actual fun equals(a: CharArray?, b: CharArray?): Boolean = a.contentEquals(b)
        actual fun equals(a: ShortArray?, b: ShortArray?): Boolean = a.contentEquals(b)
        actual fun equals(a: IntArray?, b: IntArray?): Boolean = a.contentEquals(b)
        actual fun equals(a: LongArray?, b: LongArray?): Boolean = a.contentEquals(b)
        actual fun equals(a: FloatArray?, b: FloatArray?): Boolean = a.contentEquals(b)
        actual fun equals(a: DoubleArray?, b: DoubleArray?): Boolean = a.contentEquals(b)
        actual fun equals(a: BooleanArray?, b: BooleanArray?): Boolean = a.contentEquals(b)

        actual fun fill(a: Array<Any?>, value: Any?) = a.fill(value)
        actual fun fill(a: Array<Any?>, fromIndex: Int, toIndex: Int, value: Any?) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: ByteArray, value: Byte) = a.fill(value)
        actual fun fill(a: ByteArray, fromIndex: Int, toIndex: Int, value: Byte) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: CharArray, value: Char) = a.fill(value)
        actual fun fill(a: CharArray, fromIndex: Int, toIndex: Int, value: Char) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: ShortArray, value: Short) = a.fill(value)
        actual fun fill(a: ShortArray, fromIndex: Int, toIndex: Int, value: Short) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: IntArray, value: Int) = a.fill(value)
        actual fun fill(a: IntArray, fromIndex: Int, toIndex: Int, value: Int) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: LongArray, value: Long) = a.fill(value)
        actual fun fill(a: LongArray, fromIndex: Int, toIndex: Int, value: Long) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: FloatArray, value: Float) = a.fill(value)
        actual fun fill(a: FloatArray, fromIndex: Int, toIndex: Int, value: Float) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: DoubleArray, value: Double) = a.fill(value)
        actual fun fill(a: DoubleArray, fromIndex: Int, toIndex: Int, value: Double) = a.fill(value, fromIndex, toIndex)
        actual fun fill(a: BooleanArray, value: Boolean) = a.fill(value)
        actual fun fill(a: BooleanArray, fromIndex: Int, toIndex: Int, value: Boolean) = a.fill(value, fromIndex, toIndex)

        actual fun copyOf(original: ByteArray, newLength: Int): ByteArray = original.copyOf(newLength)
        actual fun copyOf(original: CharArray, newLength: Int): CharArray = original.copyOf(newLength)
        actual fun copyOf(original: ShortArray, newLength: Int): ShortArray = original.copyOf(newLength)
        actual fun copyOf(original: IntArray, newLength: Int): IntArray = original.copyOf(newLength)
        actual fun copyOf(original: LongArray, newLength: Int): LongArray = original.copyOf(newLength)
        actual fun copyOf(original: FloatArray, newLength: Int): FloatArray = original.copyOf(newLength)
        actual fun copyOf(original: DoubleArray, newLength: Int): DoubleArray = original.copyOf(newLength)
        actual fun copyOf(original: BooleanArray, newLength: Int): BooleanArray = original.copyOf(newLength)

        actual fun copyOfRange(original: ByteArray, from: Int, to: Int): ByteArray = original.copyOfRange(from, to)
        actual fun copyOfRange(original: CharArray, from: Int, to: Int): CharArray = original.copyOfRange(from, to)
        actual fun copyOfRange(original: ShortArray, from: Int, to: Int): ShortArray = original.copyOfRange(from, to)
        actual fun copyOfRange(original: IntArray, from: Int, to: Int): IntArray = original.copyOfRange(from, to)
        actual fun copyOfRange(original: LongArray, from: Int, to: Int): LongArray = original.copyOfRange(from, to)
        actual fun copyOfRange(original: FloatArray, from: Int, to: Int): FloatArray = original.copyOfRange(from, to)
        actual fun copyOfRange(original: DoubleArray, from: Int, to: Int): DoubleArray = original.copyOfRange(from, to)
        actual fun copyOfRange(original: BooleanArray, from: Int, to: Int): BooleanArray = original.copyOfRange(from, to)

        actual fun hashCode(a: Array<*>?): Int = a.contentHashCode()
        actual fun hashCode(a: ByteArray?): Int = a.contentHashCode()
        actual fun hashCode(a: CharArray?): Int = a.contentHashCode()
        actual fun hashCode(a: ShortArray?): Int = a.contentHashCode()
        actual fun hashCode(a: IntArray?): Int = a.contentHashCode()
        actual fun hashCode(a: LongArray?): Int = a.contentHashCode()
        actual fun hashCode(a: FloatArray?): Int = a.contentHashCode()
        actual fun hashCode(a: DoubleArray?): Int = a.contentHashCode()
        actual fun hashCode(a: BooleanArray?): Int = a.contentHashCode()

        actual fun toString(a: Array<*>?): String = a.contentToString()
        actual fun toString(a: ByteArray?): String = a.contentToString()
        actual fun toString(a: CharArray?): String = a.contentToString()
        actual fun toString(a: ShortArray?): String = a.contentToString()
        actual fun toString(a: IntArray?): String = a.contentToString()
        actual fun toString(a: LongArray?): String = a.contentToString()
        actual fun toString(a: FloatArray?): String = a.contentToString()
        actual fun toString(a: DoubleArray?): String = a.contentToString()
        actual fun toString(a: BooleanArray?): String = a.contentToString()

        actual fun deepToString(a: Array<*>?): String = a.contentDeepToString()
        actual fun deepEquals(a1: Array<*>?, a2: Array<*>?): Boolean = a1.contentDeepEquals(a2)
        actual fun deepHashCode(a: Array<*>?): Int = a.contentDeepHashCode()

        actual fun <T> spliterator(array: Array<T>): Spliterator<T> =
            spliterator(array, 0, array.size)

        actual fun <T> spliterator(array: Array<T>, startInclusive: Int, endExclusive: Int): Spliterator<T> {
            checkRange(array.size, startInclusive, endExclusive)
            return ListSpliterator(
                array.asList(),
                startInclusive,
                endExclusive,
                Spliterator.ORDERED or Spliterator.IMMUTABLE or Spliterator.SIZED or Spliterator.SUBSIZED
            )
        }

        actual fun spliterator(array: IntArray): Spliterator.OfInt =
            spliterator(array, 0, array.size)

        actual fun spliterator(array: IntArray, startInclusive: Int, endExclusive: Int): Spliterator.OfInt {
            checkRange(array.size, startInclusive, endExclusive)
            return IntArraySpliterator(array, startInclusive, endExclusive)
        }

        actual fun spliterator(array: LongArray): Spliterator.OfLong =
            spliterator(array, 0, array.size)

        actual fun spliterator(array: LongArray, startInclusive: Int, endExclusive: Int): Spliterator.OfLong {
            checkRange(array.size, startInclusive, endExclusive)
            return LongArraySpliterator(array, startInclusive, endExclusive)
        }

        actual fun spliterator(array: DoubleArray): Spliterator.OfDouble =
            spliterator(array, 0, array.size)

        actual fun spliterator(array: DoubleArray, startInclusive: Int, endExclusive: Int): Spliterator.OfDouble {
            checkRange(array.size, startInclusive, endExclusive)
            return DoubleArraySpliterator(array, startInclusive, endExclusive)
        }

        actual fun <T> stream(array: Array<T>): java.util.stream.Stream<T> =
            stream(array, 0, array.size)

        actual fun <T> stream(array: Array<T>, startInclusive: Int, endExclusive: Int): java.util.stream.Stream<T> =
            java.util.stream.StreamSupport.stream(spliterator(array, startInclusive, endExclusive), false)

        actual fun stream(array: IntArray): java.util.stream.IntStream =
            stream(array, 0, array.size)

        actual fun stream(array: IntArray, startInclusive: Int, endExclusive: Int): java.util.stream.IntStream =
            java.util.stream.StreamSupport.intStream(spliterator(array, startInclusive, endExclusive), false)

        actual fun stream(array: LongArray): java.util.stream.LongStream =
            stream(array, 0, array.size)

        actual fun stream(array: LongArray, startInclusive: Int, endExclusive: Int): java.util.stream.LongStream =
            java.util.stream.StreamSupport.longStream(spliterator(array, startInclusive, endExclusive), false)

        actual fun stream(array: DoubleArray): java.util.stream.DoubleStream =
            stream(array, 0, array.size)

        actual fun stream(array: DoubleArray, startInclusive: Int, endExclusive: Int): java.util.stream.DoubleStream =
            java.util.stream.StreamSupport.doubleStream(spliterator(array, startInclusive, endExclusive), false)

        private fun checkRange(arrayLen: Int, startInclusive: Int, endExclusive: Int) {
            if (startInclusive > endExclusive) {
                throw IllegalArgumentException("origin($startInclusive) > fence($endExclusive)")
            }
            if (startInclusive < 0) {
                throw IndexOutOfBoundsException("Index out of range: $startInclusive")
            }
            if (endExclusive > arrayLen) {
                throw IndexOutOfBoundsException("Index out of range: $endExclusive")
            }
        }
    }
}

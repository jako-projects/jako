package java.util

import java.io.Serializable
import java.lang.Cloneable
import kotlin.math.max
import kotlin.math.min

actual open class BitSet : Cloneable, Serializable {
    private var words: LongArray
    private var wordsInUse: Int = 0

    actual constructor() : this(64)

    actual constructor(nbits: Int) {
        if (nbits < 0) throw IllegalArgumentException("nbits < 0: $nbits")
        val wordCount = max(1, (nbits + 63) ushr 6)
        words = LongArray(wordCount)
        wordsInUse = 0
    }

    private constructor(words: LongArray) {
        this.words = words
        this.wordsInUse = words.size
        recalculateWordsInUse()
    }

    private fun recalculateWordsInUse() {
        var i = words.size - 1
        while (i >= 0 && words[i] == 0L) {
            i--
        }
        wordsInUse = i + 1
    }

    private fun ensureCapacity(wordIndex: Int) {
        if (wordIndex >= words.size) {
            val newCap = max(words.size * 2, wordIndex + 1)
            words = words.copyOf(newCap)
        }
    }

    actual fun get(bitIndex: Int): Boolean {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        val wordIndex = bitIndex ushr 6
        return (wordIndex < wordsInUse) && ((words[wordIndex] and (1L shl bitIndex)) != 0L)
    }

    actual fun get(fromIndex: Int, toIndex: Int): BitSet {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        if (toIndex < 0) throw IndexOutOfBoundsException("toIndex < 0: $toIndex")
        if (fromIndex > toIndex) throw IndexOutOfBoundsException("fromIndex: $fromIndex > toIndex: $toIndex")

        val len = length()
        if (len <= fromIndex || fromIndex == toIndex) {
            return BitSet(0)
        }
        val targetLen = toIndex - fromIndex
        val result = BitSet(targetLen)
        for (i in 0 until targetLen) {
            if (get(fromIndex + i)) {
                result.set(i)
            }
        }
        return result
    }

    actual fun set(bitIndex: Int) {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        val wordIndex = bitIndex ushr 6
        ensureCapacity(wordIndex)
        words[wordIndex] = words[wordIndex] or (1L shl bitIndex)
        if (wordIndex >= wordsInUse) {
            wordsInUse = wordIndex + 1
        }
    }

    actual fun set(bitIndex: Int, value: Boolean) {
        if (value) set(bitIndex) else clear(bitIndex)
    }

    actual fun set(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        if (toIndex < 0) throw IndexOutOfBoundsException("toIndex < 0: $toIndex")
        if (fromIndex > toIndex) throw IndexOutOfBoundsException("fromIndex: $fromIndex > toIndex: $toIndex")
        for (i in fromIndex until toIndex) {
            set(i)
        }
    }

    actual fun set(fromIndex: Int, toIndex: Int, value: Boolean) {
        if (value) set(fromIndex, toIndex) else clear(fromIndex, toIndex)
    }

    actual fun clear(bitIndex: Int) {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        val wordIndex = bitIndex ushr 6
        if (wordIndex >= wordsInUse) return
        words[wordIndex] = words[wordIndex] and (1L shl bitIndex).inv()
        recalculateWordsInUse()
    }

    actual fun clear(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        if (toIndex < 0) throw IndexOutOfBoundsException("toIndex < 0: $toIndex")
        if (fromIndex > toIndex) throw IndexOutOfBoundsException("fromIndex: $fromIndex > toIndex: $toIndex")
        for (i in fromIndex until toIndex) {
            clear(i)
        }
    }

    actual fun clear() {
        for (i in 0 until wordsInUse) {
            words[i] = 0L
        }
        wordsInUse = 0
    }

    actual fun flip(bitIndex: Int) {
        if (bitIndex < 0) throw IndexOutOfBoundsException("bitIndex < 0: $bitIndex")
        val wordIndex = bitIndex ushr 6
        ensureCapacity(wordIndex)
        words[wordIndex] = words[wordIndex] xor (1L shl bitIndex)
        recalculateWordsInUse()
    }

    actual fun flip(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        if (toIndex < 0) throw IndexOutOfBoundsException("toIndex < 0: $toIndex")
        if (fromIndex > toIndex) throw IndexOutOfBoundsException("fromIndex: $fromIndex > toIndex: $toIndex")
        for (i in fromIndex until toIndex) {
            flip(i)
        }
    }

    actual fun and(set: BitSet) {
        if (this === set) return
        while (wordsInUse > set.wordsInUse) {
            words[--wordsInUse] = 0L
        }
        for (i in 0 until wordsInUse) {
            words[i] = words[i] and set.words[i]
        }
        recalculateWordsInUse()
    }

    actual fun or(set: BitSet) {
        if (this === set) return
        val common = min(wordsInUse, set.wordsInUse)
        if (wordsInUse < set.wordsInUse) {
            ensureCapacity(set.wordsInUse - 1)
            for (i in wordsInUse until set.wordsInUse) {
                words[i] = set.words[i]
            }
            wordsInUse = set.wordsInUse
        }
        for (i in 0 until common) {
            words[i] = words[i] or set.words[i]
        }
        recalculateWordsInUse()
    }

    actual fun xor(set: BitSet) {
        val common = min(wordsInUse, set.wordsInUse)
        if (wordsInUse < set.wordsInUse) {
            ensureCapacity(set.wordsInUse - 1)
            for (i in wordsInUse until set.wordsInUse) {
                words[i] = set.words[i]
            }
            wordsInUse = set.wordsInUse
        }
        for (i in 0 until common) {
            words[i] = words[i] xor set.words[i]
        }
        recalculateWordsInUse()
    }

    actual fun andNot(set: BitSet) {
        val common = min(wordsInUse, set.wordsInUse)
        for (i in 0 until common) {
            words[i] = words[i] and set.words[i].inv()
        }
        recalculateWordsInUse()
    }

    actual fun intersects(set: BitSet): Boolean {
        val common = min(wordsInUse, set.wordsInUse)
        for (i in 0 until common) {
            if ((words[i] and set.words[i]) != 0L) return true
        }
        return false
    }

    actual fun cardinality(): Int {
        var sum = 0
        for (i in 0 until wordsInUse) {
            sum += words[i].countOneBits()
        }
        return sum
    }

    actual fun isEmpty(): Boolean = wordsInUse == 0

    actual val isEmpty: Boolean get() = isEmpty()

    actual fun length(): Int {
        if (wordsInUse == 0) return 0
        val highestWord = words[wordsInUse - 1]
        return ((wordsInUse - 1) shl 6) + (64 - highestWord.countLeadingZeroBits())
    }

    actual fun size(): Int = words.size shl 6

    actual fun toByteArray(): ByteArray {
        val len = length()
        val byteCount = (len + 7) ushr 3
        val bytes = ByteArray(byteCount)
        for (i in 0 until byteCount) {
            val wordIndex = i ushr 3
            if (wordIndex < wordsInUse) {
                bytes[i] = ((words[wordIndex] ushr ((i and 7) shl 3)) and 0xFFL).toByte()
            }
        }
        return bytes
    }

    actual fun toLongArray(): LongArray {
        return words.copyOf(wordsInUse)
    }

    actual fun nextSetBit(fromIndex: Int): Int {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        var u = fromIndex ushr 6
        if (u >= wordsInUse) return -1
        var word = words[u] and (0L.inv() shl fromIndex)
        while (true) {
            if (word != 0L) {
                return (u shl 6) + word.countTrailingZeroBits()
            }
            if (++u >= wordsInUse) return -1
            word = words[u]
        }
    }

    actual fun nextClearBit(fromIndex: Int): Int {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex < 0: $fromIndex")
        var u = fromIndex ushr 6
        if (u >= wordsInUse) return fromIndex
        var word = words[u].inv() and (0L.inv() shl fromIndex)
        while (true) {
            if (word != 0L) {
                return (u shl 6) + word.countTrailingZeroBits()
            }
            if (++u >= wordsInUse) return wordsInUse shl 6
            word = words[u].inv()
        }
    }

    actual fun previousSetBit(fromIndex: Int): Int {
        if (fromIndex < 0) {
            if (fromIndex == -1) return -1
            throw IndexOutOfBoundsException("fromIndex < -1: $fromIndex")
        }
        var u = fromIndex ushr 6
        if (u >= wordsInUse) {
            u = wordsInUse - 1
            if (u < 0) return -1
        }
        var word = words[u] and (0L.inv() ushr (63 - fromIndex))
        while (true) {
            if (word != 0L) {
                return (u shl 6) + 63 - word.countLeadingZeroBits()
            }
            if (u-- == 0) return -1
            word = words[u]
        }
    }

    actual fun previousClearBit(fromIndex: Int): Int {
        if (fromIndex < 0) {
            if (fromIndex == -1) return -1
            throw IndexOutOfBoundsException("fromIndex < -1: $fromIndex")
        }
        var u = fromIndex ushr 6
        if (u >= wordsInUse) return fromIndex
        var word = words[u].inv() and (0L.inv() ushr (63 - fromIndex))
        while (true) {
            if (word != 0L) {
                return (u shl 6) + 63 - word.countLeadingZeroBits()
            }
            if (u-- == 0) return -1
            word = words[u].inv()
        }
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitSet) return false
        if (this.wordsInUse != other.wordsInUse) return false
        for (i in 0 until wordsInUse) {
            if (this.words[i] != other.words[i]) return false
        }
        return true
    }

    actual override fun hashCode(): Int {
        var h = 1234L
        for (i in wordsInUse - 1 downTo 0) {
            h = h xor (words[i] * (i + 1))
        }
        return ((h ushr 32) xor h).toInt()
    }

    actual override fun toString(): String {
        val sb = StringBuilder("{")
        var first = true
        var i = nextSetBit(0)
        while (i >= 0) {
            if (!first) sb.append(", ")
            sb.append(i)
            first = false
            i = nextSetBit(i + 1)
        }
        sb.append("}")
        return sb.toString()
    }

    actual companion object {
        actual fun valueOf(bytes: ByteArray): BitSet {
            val words = LongArray((bytes.size + 7) ushr 3)
            for (i in bytes.indices) {
                val wordIndex = i ushr 3
                val byteVal = (bytes[i].toLong() and 0xFFL)
                words[wordIndex] = words[wordIndex] or (byteVal shl ((i and 7) shl 3))
            }
            return BitSet(words)
        }

        actual fun valueOf(longs: LongArray): BitSet {
            return BitSet(longs.copyOf())
        }
    }
}

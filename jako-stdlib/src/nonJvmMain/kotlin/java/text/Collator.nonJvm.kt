package java.text

import java.lang.Cloneable
import java.util.Locale

internal class NativeCollationKey(
    source: String,
    private val keyBytes: ByteArray
) : CollationKey(source) {
    override fun compareTo(other: CollationKey): Int {
        val otherBytes = (other as? NativeCollationKey)?.keyBytes ?: other.toByteArray()
        val minLen = minOf(keyBytes.size, otherBytes.size)
        for (i in 0 until minLen) {
            val a = keyBytes[i].toInt() and 0xFF
            val b = otherBytes[i].toInt() and 0xFF
            if (a != b) return a.compareTo(b)
        }
        return keyBytes.size.compareTo(otherBytes.size)
    }

    override fun toByteArray(): ByteArray = keyBytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollationKey) return false
        return compareTo(other) == 0
    }

    override fun hashCode(): Int = keyBytes.contentHashCode()
}

actual abstract class Collator protected actual constructor() : Comparator<Any>, Cloneable {
    private var strengthValue: Int = TERTIARY
    private var decompositionValue: Int = CANONICAL_DECOMPOSITION

    actual abstract fun compare(source: String, target: String): Int

    actual open override fun compare(o1: Any, o2: Any): Int =
        compare(o1 as String, o2 as String)

    actual abstract fun getCollationKey(source: String): CollationKey

    actual open fun equals(source: String, target: String): Boolean =
        compare(source, target) == 0

    actual open fun getStrength(): Int = strengthValue

    actual open fun setStrength(newStrength: Int) {
        if (newStrength != PRIMARY && newStrength != SECONDARY && newStrength != TERTIARY && newStrength != IDENTICAL) {
            throw IllegalArgumentException("Incorrect comparison level.")
        }
        strengthValue = newStrength
    }

    actual open fun getDecomposition(): Int = decompositionValue

    actual open fun setDecomposition(decompositionMode: Int) {
        if (decompositionMode != NO_DECOMPOSITION &&
            decompositionMode != CANONICAL_DECOMPOSITION &&
            decompositionMode != FULL_DECOMPOSITION
        ) {
            throw IllegalArgumentException("Wrong decomposition mode.")
        }
        decompositionValue = decompositionMode
    }

    actual open fun clone(): Any {
        val copy = RuleBasedNativeCollator()
        copy.setStrength(strengthValue)
        copy.setDecomposition(decompositionValue)
        return copy
    }

    actual companion object {
        actual val PRIMARY: Int = 0
        actual val SECONDARY: Int = 1
        actual val TERTIARY: Int = 2
        actual val IDENTICAL: Int = 3
        actual val NO_DECOMPOSITION: Int = 0
        actual val CANONICAL_DECOMPOSITION: Int = 1
        actual val FULL_DECOMPOSITION: Int = 2

        actual fun getInstance(): Collator = getInstance(Locale.getDefault())

        actual fun getInstance(desiredLocale: Locale): Collator = RuleBasedNativeCollator()

        actual fun getAvailableLocales(): Array<Locale> =
            arrayOf(Locale.US, Locale.UK, Locale.ROOT, Locale.ENGLISH, Locale.CHINA)
    }
}

internal class RuleBasedNativeCollator : Collator() {
    private fun normalizeInput(s: String): String = when (getDecomposition()) {
        NO_DECOMPOSITION -> s
        CANONICAL_DECOMPOSITION -> Normalizer.normalize(s, Normalizer.Form.NFD)
        else -> Normalizer.normalize(s, Normalizer.Form.NFKD)
    }

    private fun stripCombiningMarks(s: String): String {
        val nfd = Normalizer.normalize(s, Normalizer.Form.NFD)
        val sb = StringBuilder(nfd.length)
        for (ch in nfd) {
            if (ch.code !in 0x0300..0x036F) {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    override fun compare(source: String, target: String): Int {
        val strength = getStrength()
        val normSource = normalizeInput(source)
        val normTarget = normalizeInput(target)

        // Level 1 (PRIMARY): base characters, case-insensitive, stripped of accents
        val base1 = stripCombiningMarks(normSource).lowercase()
        val base2 = stripCombiningMarks(normTarget).lowercase()
        val cmp1 = base1.compareTo(base2)
        if (cmp1 != 0 || strength == PRIMARY) return cmp1

        // Level 2 (SECONDARY): accents/diacritics, case-insensitive
        val sec1 = normSource.lowercase()
        val sec2 = normTarget.lowercase()
        val cmp2 = sec1.compareTo(sec2)
        if (cmp2 != 0 || strength == SECONDARY) return cmp2

        // Level 3 (TERTIARY): case difference (lowercase < uppercase for same letter)
        val minLen = minOf(normSource.length, normTarget.length)
        for (i in 0 until minLen) {
            val c1 = normSource[i]
            val c2 = normTarget[i]
            if (c1 != c2) {
                val l1 = c1.lowercaseChar()
                val l2 = c2.lowercaseChar()
                if (l1 == l2) {
                    // Lowercase sorts before uppercase in standard collation
                    return if (c1.isLowerCase() && c2.isUpperCase()) -1
                    else if (c1.isUpperCase() && c2.isLowerCase()) 1
                    else c1.compareTo(c2)
                }
                return c1.compareTo(c2)
            }
        }
        val lenCmp = normSource.length.compareTo(normTarget.length)
        if (lenCmp != 0 || strength == TERTIARY) return lenCmp

        // Level 4 (IDENTICAL): normalized codepoint comparison
        return normSource.compareTo(normTarget)
    }

    override fun getCollationKey(source: String): CollationKey {
        val strength = getStrength()
        val norm = normalizeInput(source)
        val base = stripCombiningMarks(norm).lowercase()
        val out = ArrayList<Byte>()

        fun appendUtf8(s: String) {
            for (b in s.encodeToByteArray()) {
                out.add(if (b == 0.toByte()) 1.toByte() else b)
            }
        }

        appendUtf8(base)
        if (strength >= SECONDARY) {
            out.add(0)
            appendUtf8(norm.lowercase())
        }
        if (strength >= TERTIARY) {
            out.add(0)
            for (ch in norm) {
                out.add(if (ch.isUpperCase()) 2.toByte() else 1.toByte())
            }
        }
        if (strength >= IDENTICAL) {
            out.add(0)
            appendUtf8(norm)
        }
        return NativeCollationKey(source, out.toByteArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuleBasedNativeCollator) return false
        return getStrength() == other.getStrength() && getDecomposition() == other.getDecomposition()
    }

    override fun hashCode(): Int = getStrength() * 31 + getDecomposition()
}

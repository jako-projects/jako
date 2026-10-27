package java.math

import java.io.Serializable

actual class MathContext : Serializable {
    actual val precision: Int
    actual val roundingMode: RoundingMode

    actual constructor(precision: Int) : this(precision, RoundingMode.HALF_UP)

    actual constructor(precision: Int, setRoundingMode: RoundingMode) {
        if (precision < 0) {
            throw IllegalArgumentException("Digits < 0")
        }
        this.precision = precision
        this.roundingMode = setRoundingMode
    }

    actual constructor(valStr: String) {
        if (!valStr.startsWith("precision=")) {
            throw IllegalArgumentException("bad string format")
        }
        val spaceIdx = valStr.indexOf(' ')
        if (spaceIdx == -1) {
            throw IllegalArgumentException("bad string format")
        }
        val precSub = valStr.substring(10, spaceIdx)
        val rest = valStr.substring(spaceIdx + 1)
        if (!rest.startsWith("roundingMode=")) {
            throw IllegalArgumentException("bad string format")
        }
        val roundSub = rest.substring(13)
        val parsedPrecision = precSub.toIntOrNull() ?: throw IllegalArgumentException("bad string format")
        if (parsedPrecision < 0) {
            throw IllegalArgumentException("Digits < 0")
        }
        this.precision = parsedPrecision
        this.roundingMode = try {
            RoundingMode.valueOf(roundSub)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("bad string format")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MathContext) return false
        return this.precision == other.precision && this.roundingMode == other.roundingMode
    }

    override fun hashCode(): Int {
        return precision + roundingMode.hashCode() * 77
    }

    override fun toString(): String {
        return "precision=$precision roundingMode=$roundingMode"
    }

    actual companion object {
        actual val UNLIMITED: MathContext = MathContext(0, RoundingMode.HALF_UP)
        actual val DECIMAL32: MathContext = MathContext(7, RoundingMode.HALF_EVEN)
        actual val DECIMAL64: MathContext = MathContext(16, RoundingMode.HALF_EVEN)
        actual val DECIMAL128: MathContext = MathContext(34, RoundingMode.HALF_EVEN)
    }
}

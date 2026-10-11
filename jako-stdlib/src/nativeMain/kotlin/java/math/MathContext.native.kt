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
        val trimmed = valStr.trim()
        val precisionIdx = trimmed.indexOf("precision=")
        val roundingIdx = trimmed.indexOf("roundingMode=")
        if (precisionIdx == -1 || roundingIdx == -1) {
            throw IllegalArgumentException("bad string format")
        }
        val precSub = trimmed.substring(precisionIdx + 10, roundingIdx).trim()
        val roundSub = trimmed.substring(roundingIdx + 13).trim()
        this.precision = precSub.toInt()
        this.roundingMode = RoundingMode.valueOf(roundSub)
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

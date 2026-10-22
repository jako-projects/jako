package java.lang

actual object Integer {
    actual const val MAX_VALUE: Int = Int.MAX_VALUE
    actual const val MIN_VALUE: Int = Int.MIN_VALUE

    actual fun parseInt(s: String): Int = s.toInt()

    actual fun parseInt(s: String, radix: Int): Int = s.toInt(radix)

    actual fun toHexString(i: Int): String = i.toUInt().toString(16)

    actual fun toBinaryString(i: Int): String = i.toUInt().toString(2)

    actual fun compare(x: Int, y: Int): Int = x.compareTo(y)

    actual fun numberOfLeadingZeros(i: Int): Int = i.countLeadingZeroBits()

    actual fun valueOf(i: Int): Int = i

    actual fun valueOf(s: String): Int = s.toInt()

    actual fun valueOf(s: String, radix: Int): Int = s.toInt(radix)
}

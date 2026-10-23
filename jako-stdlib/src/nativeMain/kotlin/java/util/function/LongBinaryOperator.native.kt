package java.util.function

actual fun interface LongBinaryOperator {
    actual fun applyAsLong(left: Long, right: Long): Long
}

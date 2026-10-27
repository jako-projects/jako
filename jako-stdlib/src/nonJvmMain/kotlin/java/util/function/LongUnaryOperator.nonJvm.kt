package java.util.function

actual fun interface LongUnaryOperator {
    actual fun applyAsLong(operand: Long): Long
}

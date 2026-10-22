package java.util.function

actual fun interface IntUnaryOperator {
    actual fun applyAsInt(operand: Int): Int
}

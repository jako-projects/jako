package java.util.function

actual fun interface IntBinaryOperator {
    actual fun applyAsInt(left: Int, right: Int): Int
}

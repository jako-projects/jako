package java.util.function

actual fun interface IntToLongFunction {
    actual fun applyAsLong(value: Int): Long
}

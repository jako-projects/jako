package java.util.function

actual fun interface LongToIntFunction {
    actual fun applyAsInt(value: Long): Int
}

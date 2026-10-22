package java.util.function

actual fun interface LongPredicate {
    actual fun test(value: Long): Boolean
}

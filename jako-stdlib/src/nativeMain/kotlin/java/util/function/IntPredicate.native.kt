package java.util.function

actual fun interface IntPredicate {
    actual fun test(value: Int): Boolean
}

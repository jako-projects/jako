package java.util.function

actual fun interface DoublePredicate {
    actual fun test(value: Double): Boolean
}

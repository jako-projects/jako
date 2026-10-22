package java.util.function

actual fun interface ToDoubleBiFunction<T, U> {
    actual fun applyAsDouble(t: T, u: U): Double
}

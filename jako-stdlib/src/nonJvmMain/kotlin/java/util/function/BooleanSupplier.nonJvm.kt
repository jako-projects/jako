package java.util.function

actual fun interface BooleanSupplier {
    actual fun getAsBoolean(): Boolean
}

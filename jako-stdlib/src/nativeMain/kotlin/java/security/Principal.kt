package java.security

actual interface Principal {
    actual fun getName(): String
    actual val name: String get() = getName()
}

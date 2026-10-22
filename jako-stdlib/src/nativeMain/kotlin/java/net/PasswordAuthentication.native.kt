package java.net

actual class PasswordAuthentication actual constructor(
    actual val userName: String,
    actual val password: CharArray
) {
    actual fun getUserName(): String = userName

    actual fun getPassword(): CharArray = password.copyOf()
}

@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect class PasswordAuthentication(userName: String, password: CharArray) {
    fun getUserName(): String
    fun getPassword(): CharArray
    val userName: String
    val password: CharArray
}

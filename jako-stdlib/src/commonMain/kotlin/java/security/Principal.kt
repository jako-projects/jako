@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

expect interface Principal {
    fun getName(): String
    open val name: String
}

@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.io.InputStream

expect open class KeyStore {
    protected constructor()
    open fun load(stream: InputStream?, password: CharArray?)
    open fun getType(): String
    open val type: String

    companion object {
        fun getInstance(type: String): KeyStore
        fun getDefaultType(): String
    }
}

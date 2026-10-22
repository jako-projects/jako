@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.lang.Class

expect interface SocketOption<T> {
    fun name(): String
    fun type(): Class<T>
}

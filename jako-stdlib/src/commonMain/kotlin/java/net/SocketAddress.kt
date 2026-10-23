@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.Serializable

expect open class SocketAddress : Serializable {
    constructor()
}

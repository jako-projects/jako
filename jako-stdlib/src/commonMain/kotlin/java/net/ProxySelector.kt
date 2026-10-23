@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.IOException

expect abstract class ProxySelector {
    constructor()
    abstract fun select(uri: URI?): List<Proxy>
    abstract fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?)

    companion object {
        fun getDefault(): ProxySelector?
        fun setDefault(ps: ProxySelector?)
    }
}

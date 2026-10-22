@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class Proxy {
    constructor(type: Type, sa: SocketAddress)
    enum class Type { DIRECT, HTTP, SOCKS }
    fun type(): Type
    fun address(): SocketAddress?

    companion object {
        val NO_PROXY: Proxy
    }
}

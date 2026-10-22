@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class InetSocketAddress : SocketAddress {
    constructor(port: Int)
    constructor(hostname: String, port: Int)
    constructor(address: InetAddress, port: Int)
    open fun getPort(): Int
    open fun getAddress(): InetAddress?
    open fun getHostName(): String
    open fun getHostString(): String
    open fun isUnresolved(): Boolean

    open val port: Int
    open val address: InetAddress?
    open val hostName: String
    open val hostString: String
    open val isUnresolved: Boolean

    companion object {
        fun createUnresolved(host: String, port: Int): InetSocketAddress
    }
}

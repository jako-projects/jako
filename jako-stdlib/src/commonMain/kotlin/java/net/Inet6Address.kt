@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class Inet6Address : InetAddress {
    companion object {
        fun getByName(host: String?): Inet6Address
        fun getByAddress(addr: ByteArray): Inet6Address
        fun getByAddress(host: String?, addr: ByteArray): Inet6Address
    }
}

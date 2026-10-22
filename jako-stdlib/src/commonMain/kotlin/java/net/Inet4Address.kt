@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class Inet4Address : InetAddress {
    companion object {
        fun getByName(host: String?): Inet4Address
        fun getByAddress(addr: ByteArray): Inet4Address
        fun getByAddress(host: String?, addr: ByteArray): Inet4Address
    }
}

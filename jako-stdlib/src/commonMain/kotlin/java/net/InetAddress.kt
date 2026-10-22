@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.io.Serializable

expect open class InetAddress : Serializable {
    open fun getHostName(): String
    open fun getHostAddress(): String
    open fun getAddress(): ByteArray

    open val hostName: String
    open val hostAddress: String
    open val address: ByteArray

    companion object {
        fun getByName(host: String?): InetAddress
        fun getAllByName(host: String?): Array<InetAddress>
        fun getByAddress(addr: ByteArray): InetAddress
        fun getByAddress(host: String?, addr: ByteArray): InetAddress
        fun getLocalHost(): InetAddress
    }
}

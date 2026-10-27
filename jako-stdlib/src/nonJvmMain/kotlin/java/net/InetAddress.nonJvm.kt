package java.net

import java.io.Serializable

actual open class InetAddress(
    internal val hostNameStr: String?,
    internal val addressBytes: ByteArray
) : Serializable {
    actual open fun getHostName(): String = hostNameStr ?: getHostAddress()
    actual open fun getHostAddress(): String {
        return if (addressBytes.size == 4) {
            "${addressBytes[0].toInt() and 0xFF}.${addressBytes[1].toInt() and 0xFF}.${addressBytes[2].toInt() and 0xFF}.${addressBytes[3].toInt() and 0xFF}"
        } else {
            val words = IntArray(8) { i ->
                ((addressBytes[i * 2].toInt() and 0xFF) shl 8) or (addressBytes[i * 2 + 1].toInt() and 0xFF)
            }
            words.joinToString(":") { it.toString(16) }
        }
    }
    actual open fun getAddress(): ByteArray = addressBytes.copyOf()

    actual open val hostName: String get() = getHostName()
    actual open val hostAddress: String get() = getHostAddress()
    actual open val address: ByteArray get() = getAddress()

    open fun isLoopbackAddress(): Boolean {
        if (addressBytes.size == 4) {
            return addressBytes[0] == 127.toByte()
        }
        if (addressBytes.size == 16) {
            for (i in 0..14) {
                if (addressBytes[i] != 0.toByte()) return false
            }
            return addressBytes[15] == 1.toByte()
        }
        return false
    }

    open fun isAnyLocalAddress(): Boolean {
        if (addressBytes.size == 4) {
            return addressBytes.all { it == 0.toByte() }
        }
        if (addressBytes.size == 16) {
            return addressBytes.all { it == 0.toByte() }
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InetAddress) return false
        return addressBytes.contentEquals(other.addressBytes)
    }

    override fun hashCode(): Int = addressBytes.contentHashCode()
    override fun toString(): String = "${hostNameStr ?: ""}/${getHostAddress()}"

    actual companion object {
        actual fun getByName(host: String?): InetAddress = getByNameNative(host)
        actual fun getAllByName(host: String?): Array<InetAddress> = getAllByNameNative(host)
        actual fun getByAddress(addr: ByteArray): InetAddress = getByAddress(null, addr)
        actual fun getByAddress(host: String?, addr: ByteArray): InetAddress {
            return when (addr.size) {
                4 -> Inet4Address(host, addr)
                16 -> Inet6Address(host, addr)
                else -> InetAddress(host, addr)
            }
        }
        actual fun getLocalHost(): InetAddress = getLocalHostNative()
        fun getLoopbackAddress(): InetAddress = Inet4Address("localhost", byteArrayOf(127, 0, 0, 1))
    }
}

internal expect fun getByNameNative(host: String?): InetAddress

internal expect fun getAllByNameNative(host: String?): Array<InetAddress>

internal expect fun getLocalHostNative(): InetAddress

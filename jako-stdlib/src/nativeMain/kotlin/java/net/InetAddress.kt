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
internal expect fun serverSocketHasPendingConnection(serverSocket: ServerSocket): Boolean

actual open class Inet4Address(hostName: String?, addr: ByteArray) : InetAddress(hostName, addr) {
    actual companion object {
        actual fun getByName(host: String?): Inet4Address = InetAddress.getByName(host) as Inet4Address
        actual fun getByAddress(addr: ByteArray): Inet4Address = InetAddress.getByAddress(addr) as Inet4Address
        actual fun getByAddress(host: String?, addr: ByteArray): Inet4Address = InetAddress.getByAddress(host, addr) as Inet4Address
    }
}

actual open class Inet6Address(hostName: String?, addr: ByteArray) : InetAddress(hostName, addr) {
    actual companion object {
        actual fun getByName(host: String?): Inet6Address = InetAddress.getByName(host) as Inet6Address
        actual fun getByAddress(addr: ByteArray): Inet6Address = InetAddress.getByAddress(addr) as Inet6Address
        actual fun getByAddress(host: String?, addr: ByteArray): Inet6Address = InetAddress.getByAddress(host, addr) as Inet6Address
    }
}

internal fun parseIPv4(host: String): ByteArray? {
    val parts = host.split('.')
    if (parts.size != 4) return null
    val bytes = ByteArray(4)
    for (i in 0 until 4) {
        val p = parts[i].toIntOrNull() ?: return null
        if (p !in 0..255) return null
        bytes[i] = p.toByte()
    }
    return bytes
}

internal fun parseIPv6(input: String): ByteArray? {
    if (input.isEmpty() || input.length > 39) return null
    val address = ByteArray(16)
    var b = 0
    var compress = -1
    var groupOffset = -1

    var i = 0
    val limit = input.length
    while (i < limit) {
        if (b == address.size) return null

        if (i + 2 <= limit && input.startsWith("::", startIndex = i)) {
            if (compress != -1) return null
            i += 2
            b += 2
            compress = b
            if (i == limit) break
        } else if (b != 0) {
            if (input.startsWith(":", startIndex = i)) {
                i++
            } else if (input.startsWith(".", startIndex = i)) {
                if (!parseIPv4Suffix(input, groupOffset, limit, address, b - 2)) return null
                b += 2
                break
            } else {
                return null
            }
        }

        var value = 0
        groupOffset = i
        while (i < limit) {
            val c = input[i]
            val hex = when (c) {
                in '0'..'9' -> c - '0'
                in 'a'..'f' -> c - 'a' + 10
                in 'A'..'F' -> c - 'A' + 10
                else -> -1
            }
            if (hex == -1) break
            value = (value shl 4) + hex
            i++
        }
        val groupLength = i - groupOffset
        if (groupLength == 0 || groupLength > 4) return null

        address[b++] = (value.ushr(8) and 0xff).toByte()
        address[b++] = (value and 0xff).toByte()
    }

    if (b != address.size) {
        if (compress == -1) return null
        address.copyInto(address, address.size - (b - compress), compress, b)
        address.fill(0.toByte(), compress, compress + (address.size - b))
    }

    return address
}

private fun parseIPv4Suffix(
    input: String,
    pos: Int,
    limit: Int,
    address: ByteArray,
    addressOffset: Int,
): Boolean {
    var b = addressOffset
    var i = pos
    while (i < limit) {
        if (b == address.size) return false
        if (b != addressOffset) {
            if (input[i] != '.') return false
            i++
        }
        var value = 0
        val groupOffset = i
        while (i < limit) {
            val c = input[i]
            if (c < '0' || c > '9') break
            if (value == 0 && groupOffset != i) return false
            value = value * 10 + (c - '0')
            if (value > 255) return false
            i++
        }
        if (i == groupOffset) return false
        address[b++] = value.toByte()
    }
    return b == addressOffset + 4
}

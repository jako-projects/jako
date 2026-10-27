package java.net

internal expect fun serverSocketHasPendingConnection(serverSocket: ServerSocket): Boolean

actual open class Inet4Address(hostName: String?, addr: ByteArray) : InetAddress(hostName, addr) {
    actual companion object {
        actual fun getByName(host: String?): Inet4Address = InetAddress.getByName(host) as Inet4Address
        actual fun getByAddress(addr: ByteArray): Inet4Address = InetAddress.getByAddress(addr) as Inet4Address
        actual fun getByAddress(host: String?, addr: ByteArray): Inet4Address = InetAddress.getByAddress(host, addr) as Inet4Address
    }
}

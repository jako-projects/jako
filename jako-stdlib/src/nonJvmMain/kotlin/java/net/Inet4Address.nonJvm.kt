package java.net

internal expect fun serverSocketHasPendingConnection(serverSocket: ServerSocket): Boolean

actual open class Inet4Address internal constructor(hostName: String?, addr: ByteArray) : InetAddress(hostName, addr)

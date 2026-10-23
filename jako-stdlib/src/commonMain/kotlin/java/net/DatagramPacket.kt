@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

/**
 * [Nullability Decision] DatagramPacket address is nullable before being set or received; data buffer is non-null.
 */
expect class DatagramPacket {
    constructor(buf: ByteArray, offset: Int, length: Int)
    constructor(buf: ByteArray, length: Int)
    constructor(buf: ByteArray, offset: Int, length: Int, address: InetAddress?, port: Int)
    constructor(buf: ByteArray, offset: Int, length: Int, address: SocketAddress)
    constructor(buf: ByteArray, length: Int, address: InetAddress?, port: Int)
    constructor(buf: ByteArray, length: Int, address: SocketAddress)

    fun getAddress(): InetAddress?
    var address: InetAddress?
    fun getData(): ByteArray
    var data: ByteArray
    fun getOffset(): Int
    val offset: Int
    fun getLength(): Int
    var length: Int
    fun setData(buf: ByteArray, offset: Int, length: Int)
    fun setAddress(iaddr: InetAddress?)
    fun setPort(iport: Int)
    fun getPort(): Int
    var port: Int
    fun setSocketAddress(address: SocketAddress)
    fun getSocketAddress(): SocketAddress
    var socketAddress: SocketAddress
    fun setData(buf: ByteArray)
    fun setLength(length: Int)
}

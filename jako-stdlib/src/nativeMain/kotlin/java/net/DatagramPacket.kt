package java.net

actual class DatagramPacket {
    private var bufData: ByteArray
    private var bufOffset: Int
    private var bufLength: Int
    internal var bufCapacityLength: Int
    private var packetAddr: InetAddress? = null
    private var packetPort: Int = -1

    actual constructor(buf: ByteArray, offset: Int, length: Int) {
        if (offset < 0 || length < 0 || offset + length > buf.size) {
            throw IllegalArgumentException("illegal length or offset")
        }
        this.bufData = buf
        this.bufOffset = offset
        this.bufLength = length
        this.bufCapacityLength = length
    }

    actual constructor(buf: ByteArray, length: Int) : this(buf, 0, length)

    actual constructor(buf: ByteArray, offset: Int, length: Int, address: InetAddress?, port: Int) : this(buf, offset, length) {
        setAddress(address)
        setPort(port)
    }

    actual constructor(buf: ByteArray, offset: Int, length: Int, address: SocketAddress) : this(buf, offset, length) {
        setSocketAddress(address)
    }

    actual constructor(buf: ByteArray, length: Int, address: InetAddress?, port: Int) : this(buf, 0, length, address, port)

    actual constructor(buf: ByteArray, length: Int, address: SocketAddress) : this(buf, 0, length, address)

    actual fun getAddress(): InetAddress? = packetAddr
    actual var address: InetAddress?
        get() = getAddress()
        set(value) = setAddress(value)

    actual fun getData(): ByteArray = bufData
    actual var data: ByteArray
        get() = getData()
        set(value) = setData(value)

    actual fun getOffset(): Int = bufOffset
    actual val offset: Int get() = getOffset()

    actual fun getLength(): Int = bufLength
    actual var length: Int
        get() = getLength()
        set(value) = setLength(value)

    actual fun setData(buf: ByteArray, offset: Int, length: Int) {
        if (offset < 0 || length < 0 || offset + length > buf.size) {
            throw IllegalArgumentException("illegal length or offset")
        }
        this.bufData = buf
        this.bufOffset = offset
        this.bufLength = length
        this.bufCapacityLength = length
    }

    actual fun setAddress(iaddr: InetAddress?) {
        this.packetAddr = iaddr
    }

    actual fun setPort(iport: Int) {
        if (iport < 0 || iport > 0xFFFF) {
            throw IllegalArgumentException("Port out of range:$iport")
        }
        this.packetPort = iport
    }

    actual fun getPort(): Int = packetPort
    actual var port: Int
        get() = getPort()
        set(value) = setPort(value)

    actual fun setSocketAddress(address: SocketAddress) {
        if (address !is InetSocketAddress) {
            throw IllegalArgumentException("unsupported address type")
        }
        if (address.isUnresolved()) {
            throw IllegalArgumentException("unresolved address")
        }
        this.packetAddr = address.getAddress()
        this.packetPort = address.getPort()
    }

    actual fun getSocketAddress(): SocketAddress {
        val addr = packetAddr ?: InetAddress.getByAddress(byteArrayOf(0, 0, 0, 0))
        return InetSocketAddress(addr, if (packetPort < 0) 0 else packetPort)
    }

    actual var socketAddress: SocketAddress
        get() = getSocketAddress()
        set(value) = setSocketAddress(value)

    actual fun setData(buf: ByteArray) {
        this.bufData = buf
        this.bufOffset = 0
        this.bufLength = buf.size
        this.bufCapacityLength = buf.size
    }

    actual fun setLength(length: Int) {
        if (length < 0 || bufOffset + length > bufData.size) {
            throw IllegalArgumentException("illegal length")
        }
        this.bufLength = length
        this.bufCapacityLength = length
    }
}

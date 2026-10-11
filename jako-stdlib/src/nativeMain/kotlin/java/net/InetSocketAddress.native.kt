package java.net

actual open class InetSocketAddress : SocketAddress {

    private val hostname: String
    private val addr: InetAddress?
    private val _port: Int
    private val unresolved: Boolean

    actual constructor(port: Int) : this(InetAddress.getByName(null), port)

    actual constructor(address: InetAddress, port: Int) : super() {
        if (port !in 0..65535) {
            throw IllegalArgumentException("port out of range: $port")
        }
        this.addr = address
        this.hostname = addr.getHostName()
        this._port = port
        this.unresolved = false
    }

    actual constructor(hostname: String, port: Int) : super() {
        if (port !in 0..65535) {
            throw IllegalArgumentException("port out of range: $port")
        }
        var resolvedAddr: InetAddress? = null
        var isUnres = false
        try {
            resolvedAddr = InetAddress.getByName(hostname)
        } catch (e: Exception) {
            isUnres = true
        }
        this.hostname = hostname
        this.addr = resolvedAddr
        this._port = port
        this.unresolved = isUnres
    }

    private constructor(hostname: String, port: Int, unresolved: Boolean) : super() {
        this.hostname = hostname
        this.addr = null
        this._port = port
        this.unresolved = unresolved
    }

    actual open val port: Int get() = getPort()
    actual open val hostName: String get() = getHostName()
    val host: String get() = getHostName()
    actual open val address: InetAddress? get() = getAddress()
    actual open val hostString: String get() = getHostString()
    actual open val isUnresolved: Boolean get() = isUnresolved()

    actual open fun getPort(): Int = _port

    actual open fun getAddress(): InetAddress? = addr

    actual open fun getHostName(): String = hostname

    actual open fun getHostString(): String {
        return addr?.getHostName() ?: hostname
    }

    actual open fun isUnresolved(): Boolean = unresolved

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InetSocketAddress) return false
        if (_port != other._port) return false
        if (unresolved != other.unresolved) return false
        if (unresolved) {
            return hostname.equals(other.hostname, ignoreCase = true)
        }
        return addr == other.addr
    }

    override fun hashCode(): Int {
        if (unresolved) return hostname.lowercase().hashCode() + _port
        return (addr?.hashCode() ?: 0) + _port
    }

    override fun toString(): String {
        return if (isUnresolved()) {
            "$hostname/<unresolved>:$_port"
        } else {
            val a = addr
            if (a != null) {
                "${a.getHostName()}/${a.getHostAddress()}:$_port"
            } else {
                "$hostname:$_port"
            }
        }
    }

    actual companion object {
        actual fun createUnresolved(host: String, port: Int): InetSocketAddress {
            if (port !in 0..65535) {
                throw IllegalArgumentException("port out of range: $port")
            }
            return InetSocketAddress(host, port, unresolved = true)
        }
    }
}

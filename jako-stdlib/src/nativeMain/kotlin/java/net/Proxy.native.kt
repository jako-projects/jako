package java.net

actual open class Proxy {
    private val typeVal: Type
    private val saVal: SocketAddress?

    actual constructor(type: Type, sa: SocketAddress) {
        if (type == Type.DIRECT) {
            throw IllegalArgumentException("Use Proxy.NO_PROXY for DIRECT proxy")
        }
        this.typeVal = type
        this.saVal = sa
    }

    private constructor(type: Type, sa: SocketAddress?) {
        this.typeVal = type
        this.saVal = sa
    }

    actual enum class Type {
        DIRECT,
        HTTP,
        SOCKS
    }

    actual fun type(): Type = typeVal

    actual fun address(): SocketAddress? = saVal

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Proxy) return false
        return typeVal == other.typeVal && saVal == other.saVal
    }

    override fun hashCode(): Int {
        return typeVal.hashCode() * 31 + (saVal?.hashCode() ?: 0)
    }

    override fun toString(): String {
        return if (typeVal == Type.DIRECT) "DIRECT" else "$typeVal @ $saVal"
    }

    actual companion object {
        actual val NO_PROXY: Proxy = Proxy(Type.DIRECT, null)
    }
}

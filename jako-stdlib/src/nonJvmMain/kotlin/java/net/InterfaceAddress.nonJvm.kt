package java.net

actual open class InterfaceAddress internal constructor(
    private val addr: InetAddress,
    private val bcast: InetAddress?,
    private val maskLength: Short
) {
    actual open fun getAddress(): InetAddress = addr
    actual open val address: InetAddress
        get() = addr

    actual open fun getBroadcast(): InetAddress? = bcast
    actual open val broadcast: InetAddress?
        get() = bcast

    actual open fun getNetworkPrefixLength(): Short = maskLength
    actual open val networkPrefixLength: Short
        get() = maskLength

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InterfaceAddress) return false
        return addr == other.addr && bcast == other.bcast && maskLength == other.maskLength
    }

    actual override fun hashCode(): Int {
        return addr.hashCode() + (bcast?.hashCode() ?: 0) + maskLength.toInt()
    }

    actual override fun toString(): String {
        return "$addr/$maskLength [${bcast ?: "null"}]"
    }
}

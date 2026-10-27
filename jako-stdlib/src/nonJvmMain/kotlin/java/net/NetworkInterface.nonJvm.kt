package java.net

import java.lang.System
import java.util.Collections
import java.util.Enumeration

actual class NetworkInterface internal constructor(
    private val ifName: String,
    private val ifIndex: Int,
    private val addrs: List<InetAddress>,
    private val ifAddrs: List<InterfaceAddress>,
    private val loopbackFlag: Boolean = true,
    private val upFlag: Boolean = true,
    private val p2pFlag: Boolean = false,
    private val multicastFlag: Boolean = true,
    private val hwAddr: ByteArray? = null,
    private val mtuVal: Int = 16384,
    private val virtualFlag: Boolean = false,
    private val parentIf: NetworkInterface? = null
) {
    actual fun getName(): String = ifName
    actual val name: String
        get() = ifName

    actual fun getInetAddresses(): Enumeration<InetAddress> = Collections.enumeration(addrs)
    actual val inetAddresses: Enumeration<InetAddress>
        get() = getInetAddresses()

    actual fun getInterfaceAddresses(): List<InterfaceAddress> = ifAddrs
    actual val interfaceAddresses: List<InterfaceAddress>
        get() = ifAddrs

    actual fun getSubInterfaces(): Enumeration<NetworkInterface> = Collections.enumeration(emptyList())
    actual val subInterfaces: Enumeration<NetworkInterface>
        get() = getSubInterfaces()

    actual fun getParent(): NetworkInterface? = parentIf
    actual val parent: NetworkInterface?
        get() = parentIf

    actual fun getIndex(): Int = ifIndex
    actual val index: Int
        get() = ifIndex

    actual fun getDisplayName(): String? = ifName
    actual val displayName: String?
        get() = ifName

    actual fun isUp(): Boolean = upFlag
    actual val isUp: Boolean
        get() = upFlag

    actual fun isLoopback(): Boolean = loopbackFlag
    actual val isLoopback: Boolean
        get() = loopbackFlag

    actual fun isPointToPoint(): Boolean = p2pFlag
    actual val isPointToPoint: Boolean
        get() = p2pFlag

    actual fun supportsMulticast(): Boolean = multicastFlag

    actual fun getHardwareAddress(): ByteArray? = hwAddr?.copyOf()
    actual val hardwareAddress: ByteArray?
        get() = getHardwareAddress()

    actual fun getMTU(): Int = mtuVal
    actual val mtu: Int
        get() = mtuVal

    actual fun isVirtual(): Boolean = virtualFlag
    actual val isVirtual: Boolean
        get() = virtualFlag

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NetworkInterface) return false
        return ifName == other.ifName && addrs == other.addrs
    }

    actual override fun hashCode(): Int = ifName.hashCode()

    actual override fun toString(): String = "name:$ifName ($ifName)"

    actual companion object {
        private fun defaultInterfaces(): List<NetworkInterface> {
            val osName = System.getProperty("os.name") ?: ""
            val loName = if (osName.contains("Mac", ignoreCase = true) || osName.contains("Darwin", ignoreCase = true)) {
                "lo0"
            } else {
                "lo"
            }
            val loV4 = InetAddress.getByAddress("localhost", byteArrayOf(127, 0, 0, 1))
            val bcastV4 = InetAddress.getByAddress(null, byteArrayOf(127, -1, -1, -1))
            val ifAddrV4 = InterfaceAddress(loV4, bcastV4, 8)
            val loIf = NetworkInterface(
                ifName = loName,
                ifIndex = 1,
                addrs = listOf(loV4),
                ifAddrs = listOf(ifAddrV4),
                loopbackFlag = true,
                upFlag = true
            )
            return listOf(loIf)
        }

        actual fun getByName(name: String): NetworkInterface? {
            for (nif in defaultInterfaces()) {
                if (nif.name == name) return nif
            }
            if (name == "lo" || name == "lo0") {
                val base = defaultInterfaces().first()
                return NetworkInterface(
                    ifName = name,
                    ifIndex = base.index,
                    addrs = base.addrs,
                    ifAddrs = base.ifAddrs,
                    loopbackFlag = true,
                    upFlag = true
                )
            }
            return null
        }

        actual fun getByIndex(index: Int): NetworkInterface? {
            return defaultInterfaces().firstOrNull { it.index == index }
        }

        actual fun getByInetAddress(addr: InetAddress): NetworkInterface? {
            return defaultInterfaces().firstOrNull { nif ->
                nif.addrs.any { it.hostAddress == addr.hostAddress }
            }
        }

        actual fun getNetworkInterfaces(): Enumeration<NetworkInterface>? {
            return Collections.enumeration(defaultInterfaces())
        }
    }
}

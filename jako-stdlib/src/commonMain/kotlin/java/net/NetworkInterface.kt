@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import java.util.Enumeration

expect class NetworkInterface {
    fun getName(): String
    val name: String
    fun getInetAddresses(): Enumeration<InetAddress>
    val inetAddresses: Enumeration<InetAddress>
    fun getInterfaceAddresses(): List<InterfaceAddress>
    val interfaceAddresses: List<InterfaceAddress>
    fun getSubInterfaces(): Enumeration<NetworkInterface>
    val subInterfaces: Enumeration<NetworkInterface>
    fun getParent(): NetworkInterface?
    val parent: NetworkInterface?
    fun getIndex(): Int
    val index: Int
    fun getDisplayName(): String?
    val displayName: String?
    fun isUp(): Boolean
    val isUp: Boolean
    fun isLoopback(): Boolean
    val isLoopback: Boolean
    fun isPointToPoint(): Boolean
    val isPointToPoint: Boolean
    fun supportsMulticast(): Boolean
    fun getHardwareAddress(): ByteArray?
    val hardwareAddress: ByteArray?
    fun getMTU(): Int
    val mtu: Int
    fun isVirtual(): Boolean
    val isVirtual: Boolean
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun getByName(name: String): NetworkInterface?
        fun getByIndex(index: Int): NetworkInterface?
        fun getByInetAddress(addr: InetAddress): NetworkInterface?
        fun getNetworkInterfaces(): Enumeration<NetworkInterface>?
    }
}

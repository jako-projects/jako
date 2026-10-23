@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class InterfaceAddress {
    open fun getAddress(): InetAddress
    open val address: InetAddress
    open fun getBroadcast(): InetAddress?
    open val broadcast: InetAddress?
    open fun getNetworkPrefixLength(): Short
    open val networkPrefixLength: Short
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
}

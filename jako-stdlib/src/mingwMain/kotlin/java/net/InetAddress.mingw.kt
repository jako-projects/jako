@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import kotlinx.cinterop.*
import platform.posix.*

internal actual fun getByNameNative(host: String?): InetAddress {
    val all = getAllByNameNative(host)
    if (all.isEmpty()) throw UnknownHostException(host)
    return all[0]
}

internal actual fun getAllByNameNative(host: String?): Array<InetAddress> {
    if (host.isNullOrEmpty() || host.equals("localhost", ignoreCase = true)) {
        return arrayOf(InetAddress.getLoopbackAddress())
    }

    val ipv4Bytes = parseIPv4(host)
    if (ipv4Bytes != null) {
        return arrayOf(Inet4Address(host, ipv4Bytes))
    }

    val ipv6Bytes = parseIPv6(host)
    if (ipv6Bytes != null) {
        return arrayOf(Inet6Address(host, ipv6Bytes))
    }

    throw UnknownHostException("Unable to resolve host \"$host\"")
}

internal actual fun getLocalHostNative(): InetAddress {
    return Inet4Address("localhost", byteArrayOf(127, 0, 0, 1))
}

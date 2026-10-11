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
    if (host.isNullOrEmpty()) {
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

    return memScoped {
        val hints = alloc<addrinfo>()
        memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
        hints.ai_family = AF_UNSPEC
        hints.ai_socktype = SOCK_STREAM

        val resVar = allocPointerTo<addrinfo>()
        val ret = getaddrinfo(host, null, hints.ptr, resVar.ptr)
        if (ret != 0) {
            throw UnknownHostException("Unable to resolve host \"$host\": gai_error $ret")
        }

        val results = mutableListOf<InetAddress>()
        var curr: addrinfo? = resVar.value?.pointed
        while (curr != null) {
            if (curr.ai_family == AF_INET) {
                val sa = curr.ai_addr?.reinterpret<sockaddr_in>()?.pointed
                if (sa != null) {
                    val addrInt = sa.sin_addr.s_addr
                    val b0 = (addrInt and 0xFFu).toByte()
                    val b1 = ((addrInt shr 8) and 0xFFu).toByte()
                    val b2 = ((addrInt shr 16) and 0xFFu).toByte()
                    val b3 = ((addrInt shr 24) and 0xFFu).toByte()
                    results.add(Inet4Address(host, byteArrayOf(b0, b1, b2, b3)))
                }
            } else if (curr.ai_family == AF_INET6) {
                val sa = curr.ai_addr?.reinterpret<sockaddr_in6>()?.pointed
                if (sa != null) {
                    val bytes = ByteArray(16)
                    bytes.usePinned { pinned ->
                        memcpy(pinned.addressOf(0), sa.sin6_addr.ptr, 16.convert())
                    }
                    results.add(Inet6Address(host, bytes))
                }
            }
            curr = curr.ai_next?.pointed
        }
        freeaddrinfo(resVar.value)

        if (results.isEmpty()) {
            throw UnknownHostException("No IP address found for host \"$host\"")
        }
        results.toTypedArray()
    }
}

internal actual fun getLocalHostNative(): InetAddress {
    return try {
        getByNameNative("localhost")
    } catch (e: Exception) {
        Inet4Address("localhost", byteArrayOf(127, 0, 0, 1))
    }
}

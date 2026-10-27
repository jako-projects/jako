package java.net

import com.bennyhuo.jako.NodeBridge

internal actual fun getByNameNative(host: String?): InetAddress {
    val all = getAllByNameNative(host)
    if (all.isEmpty()) throw UnknownHostException(host)
    return all[0]
}

internal actual fun getAllByNameNative(host: String?): Array<InetAddress> {
    if (host.isNullOrEmpty() || host.equals("localhost", ignoreCase = true)) {
        return arrayOf(Inet4Address(host ?: "localhost", byteArrayOf(127, 0, 0, 1)))
    }

    val ipv4Bytes = parseIPv4(host)
    if (ipv4Bytes != null) {
        return arrayOf(Inet4Address(host, ipv4Bytes))
    }

    val ipv6Bytes = parseIPv6(host)
    if (ipv6Bytes != null) {
        return arrayOf(Inet6Address(host, ipv6Bytes))
    }

    val lookupRes = NodeBridge.dnsLookupAll(host)
    if (lookupRes.startsWith("OK|")) {
        val addrStrings = lookupRes.substring(3).split('|')
        val resolved = ArrayList<InetAddress>(addrStrings.size)
        for (ipStr in addrStrings) {
            if (ipStr.isEmpty()) continue
            val v4 = parseIPv4(ipStr)
            if (v4 != null) {
                resolved.add(Inet4Address(host, v4))
                continue
            }
            val v6 = parseIPv6(ipStr)
            if (v6 != null) {
                resolved.add(Inet6Address(host, v6))
            }
        }
        if (resolved.isNotEmpty()) {
            return resolved.toTypedArray()
        }
    }

    val localHostname = NodeBridge.hostname()
    if (host.equals(localHostname, ignoreCase = true)) {
        return arrayOf(Inet4Address(host, byteArrayOf(127, 0, 0, 1)))
    }

    throw UnknownHostException("Unable to resolve host \"$host\": $lookupRes")
}

internal actual fun getLocalHostNative(): InetAddress {
    val name = NodeBridge.hostname().ifEmpty { "localhost" }
    val lookupRes = NodeBridge.dnsLookupAll(name)
    if (lookupRes.startsWith("OK|")) {
        for (ipStr in lookupRes.substring(3).split('|')) {
            val v4 = parseIPv4(ipStr)
            if (v4 != null) {
                return Inet4Address(name, v4)
            }
        }
    }
    return Inet4Address(name, byteArrayOf(127, 0, 0, 1))
}

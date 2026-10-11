package java.net

import java.io.IOException

actual abstract class ProxySelector actual constructor() {
    actual abstract fun select(uri: URI?): List<Proxy>

    actual abstract fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?)

    actual companion object {
        private var defaultSelector: ProxySelector? = DefaultNativeProxySelector()

        actual fun getDefault(): ProxySelector? = defaultSelector

        actual fun setDefault(ps: ProxySelector?) {
            defaultSelector = ps
        }
    }
}

internal class DefaultNativeProxySelector : ProxySelector() {
    override fun select(uri: URI?): List<Proxy> {
        return listOf(Proxy.NO_PROXY)
    }

    override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
        // Default no-op
    }
}

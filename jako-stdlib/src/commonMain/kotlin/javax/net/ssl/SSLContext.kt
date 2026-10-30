@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

@WebSupport(WebRuntime.NODE_JS, reason = "Blocking SSL/TLS sockets require Node.js worker_threads and net module")
expect open class SSLContext {
    protected constructor()
    open fun init(km: Array<KeyManager>?, tm: Array<TrustManager>?, sr: Any?)
    open fun getSocketFactory(): SSLSocketFactory
    open fun getProtocol(): String

    open val socketFactory: SSLSocketFactory
    open val protocol: String

    companion object {
        fun getInstance(protocol: String): SSLContext
        fun getDefault(): SSLContext
    }
}

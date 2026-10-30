@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.net.HttpURLConnection
import java.net.URL
import java.security.Principal
import java.security.cert.Certificate

@WebSupport(WebRuntime.NODE_JS, reason = "Blocking HTTPS connections over Socket require Node.js worker_threads and net module")
expect abstract class HttpsURLConnection : HttpURLConnection {
    protected constructor(url: URL?)
    abstract fun getCipherSuite(): String
    open val cipherSuite: String
    abstract fun getLocalCertificates(): Array<Certificate>?
    open val localCertificates: Array<Certificate>?
    abstract fun getServerCertificates(): Array<Certificate>
    open val serverCertificates: Array<Certificate>
    open fun getPeerPrincipal(): Principal
    open val peerPrincipal: Principal
    open fun getLocalPrincipal(): Principal?
    open val localPrincipal: Principal?
    open fun setHostnameVerifier(v: HostnameVerifier)
    open fun getHostnameVerifier(): HostnameVerifier
    open var hostnameVerifier: HostnameVerifier
    open fun setSSLSocketFactory(sf: SSLSocketFactory)
    open fun getSSLSocketFactory(): SSLSocketFactory
    open var sslSocketFactory: SSLSocketFactory

    companion object {
        fun getDefaultHostnameVerifier(): HostnameVerifier
        fun setDefaultHostnameVerifier(v: HostnameVerifier)
        fun getDefaultSSLSocketFactory(): SSLSocketFactory
        fun setDefaultSSLSocketFactory(sf: SSLSocketFactory)
    }
}

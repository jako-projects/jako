@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import java.net.Socket

expect abstract class SSLSocket : Socket {
    protected constructor()

    abstract fun startHandshake()
    abstract fun getSession(): SSLSession
    abstract fun setUseClientMode(mode: Boolean)
    abstract fun getUseClientMode(): Boolean
    open fun setNeedClientAuth(need: Boolean)
    open fun getNeedClientAuth(): Boolean
    open fun setWantClientAuth(want: Boolean)
    open fun getWantClientAuth(): Boolean
    abstract fun getEnabledProtocols(): Array<String>
    abstract fun setEnabledProtocols(protocols: Array<String>?)
    abstract fun getSupportedCipherSuites(): Array<String>
    abstract fun getEnabledCipherSuites(): Array<String>
    abstract fun setEnabledCipherSuites(suites: Array<String>?)

    open var useClientMode: Boolean
    open var needClientAuth: Boolean
    open var wantClientAuth: Boolean
    open val session: SSLSession
    open var enabledProtocols: Array<String>
    open val supportedCipherSuites: Array<String>
    open var enabledCipherSuites: Array<String>
}

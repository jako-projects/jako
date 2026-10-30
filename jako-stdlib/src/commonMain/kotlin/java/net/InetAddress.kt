@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.Serializable

expect open class InetAddress : Serializable {
    open fun getHostName(): String
    open fun getHostAddress(): String
    open fun getAddress(): ByteArray

    open val hostName: String
    open val hostAddress: String
    open val address: ByteArray

    companion object {
        @WebSupport(WebRuntime.NODE_JS, reason = "Synchronous DNS resolution requires Node.js worker_threads and dns module")
        fun getByName(host: String?): InetAddress
        @WebSupport(WebRuntime.NODE_JS, reason = "Synchronous DNS resolution requires Node.js worker_threads and dns module")
        fun getAllByName(host: String?): Array<InetAddress>
        fun getByAddress(addr: ByteArray): InetAddress
        fun getByAddress(host: String?, addr: ByteArray): InetAddress
        @WebSupport(WebRuntime.NODE_JS, reason = "Synchronous DNS resolution requires Node.js worker_threads and dns module")
        fun getLocalHost(): InetAddress
    }
}

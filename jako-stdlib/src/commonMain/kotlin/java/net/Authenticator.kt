@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect abstract class Authenticator() {
    enum class RequestorType {
        PROXY, SERVER
    }

    protected open fun getPasswordAuthentication(): PasswordAuthentication?

    protected open val requestingHost: String?
    protected open val requestingSite: InetAddress
    protected open val requestingPort: Int
    protected open val requestingProtocol: String?
    protected open val requestingPrompt: String?
    protected open val requestingScheme: String?
    protected open val requestingURL: URL?
    protected open val requestorType: RequestorType?

    protected open fun getRequestingHost(): String?
    protected open fun getRequestingSite(): InetAddress
    protected open fun getRequestingPort(): Int
    protected open fun getRequestingProtocol(): String?
    protected open fun getRequestingPrompt(): String?
    protected open fun getRequestingScheme(): String?
    protected open fun getRequestingURL(): URL?
    protected open fun getRequestorType(): RequestorType?

    companion object {
        fun setDefault(a: Authenticator?)
        fun requestPasswordAuthentication(
            host: String,
            addr: InetAddress?,
            port: Int,
            protocol: String?,
            prompt: String?,
            scheme: String?,
            url: URL?,
            requestorType: RequestorType?
        ): PasswordAuthentication?
        fun requestPasswordAuthentication(
            host: String,
            addr: InetAddress?,
            port: Int,
            protocol: String?,
            prompt: String?,
            scheme: String?
        ): PasswordAuthentication?
    }
}

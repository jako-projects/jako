package java.net

actual class PasswordAuthentication actual constructor(
    actual val userName: String,
    actual val password: CharArray
) {
    actual fun getUserName(): String = userName

    actual fun getPassword(): CharArray = password.copyOf()
}

actual abstract class Authenticator actual constructor() {
    actual enum class RequestorType {
        PROXY, SERVER
    }

    actual protected open fun getPasswordAuthentication(): PasswordAuthentication? = null

    private var _requestingHost: String? = null
    private var _requestingSite: InetAddress? = null
    private var _requestingPort: Int = -1
    private var _requestingProtocol: String? = null
    private var _requestingPrompt: String? = null
    private var _requestingScheme: String? = null
    private var _requestingURL: URL? = null
    private var _requestorType: RequestorType? = null

    actual protected open val requestingHost: String? get() = _requestingHost
    actual protected open val requestingSite: InetAddress get() = _requestingSite ?: InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))
    actual protected open val requestingPort: Int get() = _requestingPort
    actual protected open val requestingProtocol: String? get() = _requestingProtocol
    actual protected open val requestingPrompt: String? get() = _requestingPrompt
    actual protected open val requestingScheme: String? get() = _requestingScheme
    actual protected open val requestingURL: URL? get() = _requestingURL
    actual protected open val requestorType: RequestorType? get() = _requestorType

    actual protected open fun getRequestingHost(): String? = _requestingHost
    actual protected open fun getRequestingSite(): InetAddress = requestingSite
    actual protected open fun getRequestingPort(): Int = _requestingPort
    actual protected open fun getRequestingProtocol(): String? = _requestingProtocol
    actual protected open fun getRequestingPrompt(): String? = _requestingPrompt
    actual protected open fun getRequestingScheme(): String? = _requestingScheme
    actual protected open fun getRequestingURL(): URL? = _requestingURL
    actual protected open fun getRequestorType(): RequestorType? = _requestorType

    actual companion object {
        private var defaultAuthenticator: Authenticator? = null

        actual fun setDefault(a: Authenticator?) {
            defaultAuthenticator = a
        }

        actual fun requestPasswordAuthentication(
            host: String,
            addr: InetAddress?,
            port: Int,
            protocol: String?,
            prompt: String?,
            scheme: String?,
            url: URL?,
            requestorType: RequestorType?
        ): PasswordAuthentication? {
            val a = defaultAuthenticator ?: return null
            a._requestingHost = host
            a._requestingSite = addr
            a._requestingPort = port
            a._requestingProtocol = protocol
            a._requestingPrompt = prompt
            a._requestingScheme = scheme
            a._requestingURL = url
            a._requestorType = requestorType
            try {
                return a.getPasswordAuthentication()
            } finally {
                a._requestingHost = null
                a._requestingSite = null
                a._requestingPort = -1
                a._requestingProtocol = null
                a._requestingPrompt = null
                a._requestingScheme = null
                a._requestingURL = null
                a._requestorType = null
            }
        }

        actual fun requestPasswordAuthentication(
            host: String,
            addr: InetAddress?,
            port: Int,
            protocol: String?,
            prompt: String?,
            scheme: String?
        ): PasswordAuthentication? = requestPasswordAuthentication(host, addr, port, protocol, prompt, scheme, null, null)
    }
}

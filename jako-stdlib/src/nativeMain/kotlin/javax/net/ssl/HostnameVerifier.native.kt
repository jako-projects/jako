package javax.net.ssl

actual interface HostnameVerifier {
    actual fun verify(hostname: String, session: SSLSession): Boolean
}

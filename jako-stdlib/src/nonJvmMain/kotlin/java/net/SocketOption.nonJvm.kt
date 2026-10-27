package java.net

import java.lang.Class

actual interface SocketOption<T> {
    actual fun name(): String
    actual fun type(): Class<T>
}

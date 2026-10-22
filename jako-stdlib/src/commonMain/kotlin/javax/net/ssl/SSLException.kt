@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.net.ssl

import java.io.IOException

expect open class SSLException : IOException {
    constructor(reason: String?)
    constructor(reason: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

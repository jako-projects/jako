package android.util

import java.io.IOException

actual open class MalformedJsonException actual constructor(
    message: String?
) : IOException(message)

package java.util

actual open class MissingResourceException actual constructor(
    s: String?,
    className: String?,
    key: String?
) : RuntimeException(s) {
    private val classNameVal: String? = className
    private val keyVal: String? = key

    actual open fun getClassName(): String? = classNameVal
    actual open val className: String?
        get() = classNameVal

    actual open fun getKey(): String? = keyVal
    actual open val key: String?
        get() = keyVal
}

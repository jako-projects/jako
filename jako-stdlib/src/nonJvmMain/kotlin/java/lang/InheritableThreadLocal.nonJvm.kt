package java.lang

actual open class InheritableThreadLocal<T> actual constructor() : ThreadLocal<T>() {
    actual protected open fun childValue(parentValue: T?): T? = parentValue

    @Suppress("UNCHECKED_CAST")
    override fun copyForChild(parentValue: Any?): Any? {
        return childValue(parentValue as T?)
    }
}

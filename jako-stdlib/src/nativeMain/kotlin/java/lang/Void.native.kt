package java.lang

actual class Void private constructor() {
    actual companion object {
        actual val TYPE: Class<Void> = Class<Void>(Unit::class, primitiveFlag = true)
    }
}

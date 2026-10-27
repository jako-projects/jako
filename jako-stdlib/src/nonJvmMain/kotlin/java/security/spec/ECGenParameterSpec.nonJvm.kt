package java.security.spec

actual open class ECGenParameterSpec actual constructor(
    private val stdName: String
) : AlgorithmParameterSpec {
    actual open fun getName(): String = stdName
    actual open val name: String get() = stdName
}

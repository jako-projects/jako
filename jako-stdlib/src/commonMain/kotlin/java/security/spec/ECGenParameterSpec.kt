@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security.spec

expect open class ECGenParameterSpec(stdName: String) : AlgorithmParameterSpec {
    open fun getName(): String
    open val name: String
}

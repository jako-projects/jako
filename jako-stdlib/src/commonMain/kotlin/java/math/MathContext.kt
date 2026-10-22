@file:Suppress("NO_ACTUAL_FOR_EXPECT", "OVERRIDE_DEPRECATION")

package java.math

import java.io.Serializable

expect class MathContext : Serializable {
    val precision: Int
    val roundingMode: RoundingMode

    constructor(precision: Int)
    constructor(precision: Int, setRoundingMode: RoundingMode)
    constructor(valStr: String)

    companion object {
        val UNLIMITED: MathContext
        val DECIMAL32: MathContext
        val DECIMAL64: MathContext
        val DECIMAL128: MathContext
    }
}

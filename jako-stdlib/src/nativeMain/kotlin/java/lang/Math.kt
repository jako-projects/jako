package java.lang

import kotlin.math.*

actual object Math {
    actual const val PI: Double = kotlin.math.PI
    actual const val E: Double = kotlin.math.E

    actual fun min(a: Int, b: Int): Int = kotlin.math.min(a, b)
    actual fun min(a: Long, b: Long): Long = kotlin.math.min(a, b)
    actual fun min(a: Float, b: Float): Float = kotlin.math.min(a, b)
    actual fun min(a: Double, b: Double): Double = kotlin.math.min(a, b)

    actual fun max(a: Int, b: Int): Int = kotlin.math.max(a, b)
    actual fun max(a: Long, b: Long): Long = kotlin.math.max(a, b)
    actual fun max(a: Float, b: Float): Float = kotlin.math.max(a, b)
    actual fun max(a: Double, b: Double): Double = kotlin.math.max(a, b)

    actual fun abs(a: Int): Int = kotlin.math.abs(a)
    actual fun abs(a: Long): Long = kotlin.math.abs(a)
    actual fun abs(a: Float): Float = kotlin.math.abs(a)
    actual fun abs(a: Double): Double = kotlin.math.abs(a)
}


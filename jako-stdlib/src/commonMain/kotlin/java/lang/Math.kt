@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import kotlin.Long
import kotlin.Int

expect object Math {
    val PI: Double
    val E: Double
    fun min(a: Int, b: Int): Int
    fun min(a: Long, b: Long): Long
    fun min(a: Float, b: Float): Float
    fun min(a: Double, b: Double): Double
    fun max(a: Int, b: Int): Int
    fun max(a: Long, b: Long): Long
    fun max(a: Float, b: Float): Float
    fun max(a: Double, b: Double): Double
    fun abs(a: Int): Int
    fun abs(a: Long): Long
    fun abs(a: Float): Float
    fun abs(a: Double): Double
}

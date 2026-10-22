@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang.reflect

import java.lang.Class

/**
 * [Nullability Decision] Java Array.newInstance returns non-null array object;
 * Array.get returns Any? (elements of object arrays may be null).
 */
expect object Array {
    fun newInstance(componentType: Class<*>, length: Int): Any
    fun newInstance(componentType: Class<*>, vararg dimensions: Int): Any
    fun getLength(array: Any): Int
    fun get(array: Any, index: Int): Any?
    fun getBoolean(array: Any, index: Int): Boolean
    fun getByte(array: Any, index: Int): Byte
    fun getChar(array: Any, index: Int): Char
    fun getShort(array: Any, index: Int): Short
    fun getInt(array: Any, index: Int): Int
    fun getLong(array: Any, index: Int): Long
    fun getFloat(array: Any, index: Int): Float
    fun getDouble(array: Any, index: Int): Double
    fun set(array: Any, index: Int, value: Any?)
    fun setBoolean(array: Any, index: Int, z: Boolean)
    fun setByte(array: Any, index: Int, b: Byte)
    fun setChar(array: Any, index: Int, c: Char)
    fun setShort(array: Any, index: Int, s: Short)
    fun setInt(array: Any, index: Int, i: Int)
    fun setLong(array: Any, index: Int, l: Long)
    fun setFloat(array: Any, index: Int, f: Float)
    fun setDouble(array: Any, index: Int, d: Double)
}

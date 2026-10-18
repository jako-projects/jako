package java.lang.reflect

import java.lang.Class
import java.lang.NegativeArraySizeException

actual object Array {
    actual fun newInstance(componentType: Class<*>, length: Int): Any {
        if (length < 0) throw NegativeArraySizeException(length.toString())
        if (componentType.isPrimitive()) {
            return when (componentType.kClass) {
                Boolean::class -> BooleanArray(length)
                Byte::class -> ByteArray(length)
                Short::class -> ShortArray(length)
                Char::class -> CharArray(length)
                Int::class -> IntArray(length)
                Long::class -> LongArray(length)
                Float::class -> FloatArray(length)
                Double::class -> DoubleArray(length)
                Unit::class -> throw IllegalArgumentException("Cannot create array of void")
                else -> arrayOfNulls<Any?>(length)
            }
        }
        return arrayOfNulls<Any?>(length)
    }

    actual fun newInstance(componentType: Class<*>, vararg dimensions: Int): Any {
        if (dimensions.isEmpty() || dimensions.size > 255) {
            throw IllegalArgumentException("Invalid dimensions count: ${dimensions.size}")
        }
        for (d in dimensions) {
            if (d < 0) throw NegativeArraySizeException(d.toString())
        }
        return createMultiArray(componentType, dimensions, 0)
    }

    private fun createMultiArray(componentType: Class<*>, dimensions: IntArray, depth: Int): Any {
        val len = dimensions[depth]
        if (depth == dimensions.lastIndex) {
            return newInstance(componentType, len)
        }
        return kotlin.Array<Any?>(len) {
            createMultiArray(componentType, dimensions, depth + 1)
        }
    }

    actual fun getLength(array: Any): Int = when (array) {
        is BooleanArray -> array.size
        is ByteArray -> array.size
        is ShortArray -> array.size
        is CharArray -> array.size
        is IntArray -> array.size
        is LongArray -> array.size
        is FloatArray -> array.size
        is DoubleArray -> array.size
        is kotlin.Array<*> -> array.size
        else -> throw IllegalArgumentException("Argument is not an array")
    }

    private fun checkIndex(size: Int, index: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index $index out of bounds for length $size")
        }
    }

    actual fun get(array: Any, index: Int): Any? = when (array) {
        is BooleanArray -> { checkIndex(array.size, index); array[index] }
        is ByteArray -> { checkIndex(array.size, index); array[index] }
        is ShortArray -> { checkIndex(array.size, index); array[index] }
        is CharArray -> { checkIndex(array.size, index); array[index] }
        is IntArray -> { checkIndex(array.size, index); array[index] }
        is LongArray -> { checkIndex(array.size, index); array[index] }
        is FloatArray -> { checkIndex(array.size, index); array[index] }
        is DoubleArray -> { checkIndex(array.size, index); array[index] }
        is kotlin.Array<*> -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument is not an array")
    }

    actual fun getBoolean(array: Any, index: Int): Boolean = when (array) {
        is BooleanArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument is not a boolean array")
    }

    actual fun getByte(array: Any, index: Int): Byte = when (array) {
        is ByteArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument is not a byte array")
    }

    actual fun getChar(array: Any, index: Int): Char = when (array) {
        is CharArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument is not a char array")
    }

    actual fun getShort(array: Any, index: Int): Short = when (array) {
        is ByteArray -> { checkIndex(array.size, index); array[index].toShort() }
        is ShortArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument cannot be converted to short")
    }

    actual fun getInt(array: Any, index: Int): Int = when (array) {
        is ByteArray -> { checkIndex(array.size, index); array[index].toInt() }
        is ShortArray -> { checkIndex(array.size, index); array[index].toInt() }
        is CharArray -> { checkIndex(array.size, index); array[index].code }
        is IntArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument cannot be converted to int")
    }

    actual fun getLong(array: Any, index: Int): Long = when (array) {
        is ByteArray -> { checkIndex(array.size, index); array[index].toLong() }
        is ShortArray -> { checkIndex(array.size, index); array[index].toLong() }
        is CharArray -> { checkIndex(array.size, index); array[index].code.toLong() }
        is IntArray -> { checkIndex(array.size, index); array[index].toLong() }
        is LongArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument cannot be converted to long")
    }

    actual fun getFloat(array: Any, index: Int): Float = when (array) {
        is ByteArray -> { checkIndex(array.size, index); array[index].toFloat() }
        is ShortArray -> { checkIndex(array.size, index); array[index].toFloat() }
        is CharArray -> { checkIndex(array.size, index); array[index].code.toFloat() }
        is IntArray -> { checkIndex(array.size, index); array[index].toFloat() }
        is LongArray -> { checkIndex(array.size, index); array[index].toFloat() }
        is FloatArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument cannot be converted to float")
    }

    actual fun getDouble(array: Any, index: Int): Double = when (array) {
        is ByteArray -> { checkIndex(array.size, index); array[index].toDouble() }
        is ShortArray -> { checkIndex(array.size, index); array[index].toDouble() }
        is CharArray -> { checkIndex(array.size, index); array[index].code.toDouble() }
        is IntArray -> { checkIndex(array.size, index); array[index].toDouble() }
        is LongArray -> { checkIndex(array.size, index); array[index].toDouble() }
        is FloatArray -> { checkIndex(array.size, index); array[index].toDouble() }
        is DoubleArray -> { checkIndex(array.size, index); array[index] }
        else -> throw IllegalArgumentException("Argument cannot be converted to double")
    }

    @Suppress("UNCHECKED_CAST")
    actual fun set(array: Any, index: Int, value: Any?) {
        when (array) {
            is BooleanArray -> {
                checkIndex(array.size, index)
                val v = value as? Boolean ?: throw IllegalArgumentException("value is not Boolean")
                array[index] = v
            }
            is ByteArray -> {
                checkIndex(array.size, index)
                val v = value as? Byte ?: throw IllegalArgumentException("value is not Byte")
                array[index] = v
            }
            is ShortArray -> {
                checkIndex(array.size, index)
                val v = when (value) {
                    is Byte -> value.toShort()
                    is Short -> value
                    else -> throw IllegalArgumentException("value cannot be converted to Short")
                }
                array[index] = v
            }
            is CharArray -> {
                checkIndex(array.size, index)
                val v = value as? Char ?: throw IllegalArgumentException("value is not Char")
                array[index] = v
            }
            is IntArray -> {
                checkIndex(array.size, index)
                val v = when (value) {
                    is Byte -> value.toInt()
                    is Short -> value.toInt()
                    is Char -> value.code
                    is Int -> value
                    else -> throw IllegalArgumentException("value cannot be converted to Int")
                }
                array[index] = v
            }
            is LongArray -> {
                checkIndex(array.size, index)
                val v = when (value) {
                    is Byte -> value.toLong()
                    is Short -> value.toLong()
                    is Char -> value.code.toLong()
                    is Int -> value.toLong()
                    is Long -> value
                    else -> throw IllegalArgumentException("value cannot be converted to Long")
                }
                array[index] = v
            }
            is FloatArray -> {
                checkIndex(array.size, index)
                val v = when (value) {
                    is Byte -> value.toFloat()
                    is Short -> value.toFloat()
                    is Char -> value.code.toFloat()
                    is Int -> value.toFloat()
                    is Long -> value.toFloat()
                    is Float -> value
                    else -> throw IllegalArgumentException("value cannot be converted to Float")
                }
                array[index] = v
            }
            is DoubleArray -> {
                checkIndex(array.size, index)
                val v = when (value) {
                    is Byte -> value.toDouble()
                    is Short -> value.toDouble()
                    is Char -> value.code.toDouble()
                    is Int -> value.toDouble()
                    is Long -> value.toDouble()
                    is Float -> value.toDouble()
                    is Double -> value
                    else -> throw IllegalArgumentException("value cannot be converted to Double")
                }
                array[index] = v
            }
            is kotlin.Array<*> -> {
                checkIndex(array.size, index)
                (array as kotlin.Array<Any?>)[index] = value
            }
            else -> throw IllegalArgumentException("Argument is not an array")
        }
    }

    actual fun setBoolean(array: Any, index: Int, z: Boolean) {
        if (array is BooleanArray) {
            checkIndex(array.size, index)
            array[index] = z
        } else {
            throw IllegalArgumentException("Argument is not a boolean array")
        }
    }

    actual fun setByte(array: Any, index: Int, b: Byte) {
        when (array) {
            is ByteArray -> { checkIndex(array.size, index); array[index] = b }
            is ShortArray -> { checkIndex(array.size, index); array[index] = b.toShort() }
            is IntArray -> { checkIndex(array.size, index); array[index] = b.toInt() }
            is LongArray -> { checkIndex(array.size, index); array[index] = b.toLong() }
            is FloatArray -> { checkIndex(array.size, index); array[index] = b.toFloat() }
            is DoubleArray -> { checkIndex(array.size, index); array[index] = b.toDouble() }
            else -> throw IllegalArgumentException("Argument cannot store byte")
        }
    }

    actual fun setChar(array: Any, index: Int, c: Char) {
        when (array) {
            is CharArray -> { checkIndex(array.size, index); array[index] = c }
            is IntArray -> { checkIndex(array.size, index); array[index] = c.code }
            is LongArray -> { checkIndex(array.size, index); array[index] = c.code.toLong() }
            is FloatArray -> { checkIndex(array.size, index); array[index] = c.code.toFloat() }
            is DoubleArray -> { checkIndex(array.size, index); array[index] = c.code.toDouble() }
            else -> throw IllegalArgumentException("Argument cannot store char")
        }
    }

    actual fun setShort(array: Any, index: Int, s: Short) {
        when (array) {
            is ShortArray -> { checkIndex(array.size, index); array[index] = s }
            is IntArray -> { checkIndex(array.size, index); array[index] = s.toInt() }
            is LongArray -> { checkIndex(array.size, index); array[index] = s.toLong() }
            is FloatArray -> { checkIndex(array.size, index); array[index] = s.toFloat() }
            is DoubleArray -> { checkIndex(array.size, index); array[index] = s.toDouble() }
            else -> throw IllegalArgumentException("Argument cannot store short")
        }
    }

    actual fun setInt(array: Any, index: Int, i: Int) {
        when (array) {
            is IntArray -> { checkIndex(array.size, index); array[index] = i }
            is LongArray -> { checkIndex(array.size, index); array[index] = i.toLong() }
            is FloatArray -> { checkIndex(array.size, index); array[index] = i.toFloat() }
            is DoubleArray -> { checkIndex(array.size, index); array[index] = i.toDouble() }
            else -> throw IllegalArgumentException("Argument cannot store int")
        }
    }

    actual fun setLong(array: Any, index: Int, l: Long) {
        when (array) {
            is LongArray -> { checkIndex(array.size, index); array[index] = l }
            is FloatArray -> { checkIndex(array.size, index); array[index] = l.toFloat() }
            is DoubleArray -> { checkIndex(array.size, index); array[index] = l.toDouble() }
            else -> throw IllegalArgumentException("Argument cannot store long")
        }
    }

    actual fun setFloat(array: Any, index: Int, f: Float) {
        when (array) {
            is FloatArray -> { checkIndex(array.size, index); array[index] = f }
            is DoubleArray -> { checkIndex(array.size, index); array[index] = f.toDouble() }
            else -> throw IllegalArgumentException("Argument cannot store float")
        }
    }

    actual fun setDouble(array: Any, index: Int, d: Double) {
        when (array) {
            is DoubleArray -> { checkIndex(array.size, index); array[index] = d }
            else -> throw IllegalArgumentException("Argument cannot store double")
        }
    }
}

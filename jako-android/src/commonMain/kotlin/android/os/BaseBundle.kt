@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * A mapping from String keys to values of various types.
 */
expect open class BaseBundle {
    val isEmpty: Boolean

    fun size(): Int
    fun isEmpty(): Boolean
    fun clear()
    fun containsKey(key: String?): Boolean
    operator fun get(key: String?): Any?
    fun remove(key: String?)
    fun putAll(bundle: PersistableBundle)
    fun keySet(): MutableSet<String>

    fun putBoolean(key: String?, value: Boolean)
    fun putInt(key: String?, value: Int)
    fun putLong(key: String?, value: Long)
    fun putDouble(key: String?, value: Double)
    fun putString(key: String?, value: String?)
    fun putBooleanArray(key: String?, value: BooleanArray?)
    fun putIntArray(key: String?, value: IntArray?)
    fun putLongArray(key: String?, value: LongArray?)
    fun putDoubleArray(key: String?, value: DoubleArray?)
    fun putStringArray(key: String?, value: Array<String?>?)

    fun getBoolean(key: String?): Boolean
    fun getBoolean(key: String?, defaultValue: Boolean): Boolean
    fun getInt(key: String?): Int
    fun getInt(key: String?, defaultValue: Int): Int
    fun getLong(key: String?): Long
    fun getLong(key: String?, defaultValue: Long): Long
    fun getDouble(key: String?): Double
    fun getDouble(key: String?, defaultValue: Double): Double
    fun getString(key: String?): String?
    fun getString(key: String?, defaultValue: String?): String?
    fun getBooleanArray(key: String?): BooleanArray?
    fun getIntArray(key: String?): IntArray?
    fun getLongArray(key: String?): LongArray?
    fun getDoubleArray(key: String?): DoubleArray?
    fun getStringArray(key: String?): Array<String?>?
}

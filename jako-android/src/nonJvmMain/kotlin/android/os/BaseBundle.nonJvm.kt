package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import android.util.ArrayMap

@Suppress("UNCHECKED_CAST")
actual open class BaseBundle {
    internal val map: ArrayMap<String?, Any?>

    constructor() {
        map = ArrayMap()
    }

    constructor(capacity: Int) {
        map = if (capacity > 0) ArrayMap(capacity) else ArrayMap()
    }

    constructor(b: BaseBundle?) {
        map = ArrayMap()
        if (b != null) {
            map.putAll(b.map)
        }
    }

    actual val isEmpty: Boolean
        get() = map.isEmpty()

    actual fun size(): Int = map.size

    actual fun isEmpty(): Boolean = map.isEmpty()

    actual fun clear() = map.clear()

    actual fun containsKey(key: String?): Boolean = map.containsKey(key)

    actual operator fun get(key: String?): Any? = map[key]

    actual fun remove(key: String?) {
        map.remove(key)
    }

    actual fun putAll(bundle: PersistableBundle) {
        map.putAll(bundle.map)
    }

    actual fun keySet(): MutableSet<String> = map.keys as MutableSet<String>

    actual fun putBoolean(key: String?, value: Boolean) {
        map[key] = value
    }

    actual fun putInt(key: String?, value: Int) {
        map[key] = value
    }

    actual fun putLong(key: String?, value: Long) {
        map[key] = value
    }

    actual fun putDouble(key: String?, value: Double) {
        map[key] = value
    }

    actual fun putString(key: String?, value: String?) {
        map[key] = value
    }

    actual fun putBooleanArray(key: String?, value: BooleanArray?) {
        map[key] = value
    }

    actual fun putIntArray(key: String?, value: IntArray?) {
        map[key] = value
    }

    actual fun putLongArray(key: String?, value: LongArray?) {
        map[key] = value
    }

    actual fun putDoubleArray(key: String?, value: DoubleArray?) {
        map[key] = value
    }

    actual fun putStringArray(key: String?, value: Array<String?>?) {
        map[key] = value
    }

    actual fun getBoolean(key: String?): Boolean = getBoolean(key, false)

    actual fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return (map[key] as? Boolean) ?: defaultValue
    }

    actual fun getInt(key: String?): Int = getInt(key, 0)

    actual fun getInt(key: String?, defaultValue: Int): Int {
        return (map[key] as? Int) ?: defaultValue
    }

    actual fun getLong(key: String?): Long = getLong(key, 0L)

    actual fun getLong(key: String?, defaultValue: Long): Long {
        return (map[key] as? Long) ?: defaultValue
    }

    actual fun getDouble(key: String?): Double = getDouble(key, 0.0)

    actual fun getDouble(key: String?, defaultValue: Double): Double {
        return (map[key] as? Double) ?: defaultValue
    }

    actual fun getString(key: String?): String? = map[key] as? String

    actual fun getString(key: String?, defaultValue: String?): String? {
        return (map[key] as? String) ?: defaultValue
    }

    actual fun getBooleanArray(key: String?): BooleanArray? = map[key] as? BooleanArray

    actual fun getIntArray(key: String?): IntArray? = map[key] as? IntArray

    actual fun getLongArray(key: String?): LongArray? = map[key] as? LongArray

    actual fun getDoubleArray(key: String?): DoubleArray? = map[key] as? DoubleArray

    actual fun getStringArray(key: String?): Array<String?>? = map[key] as? Array<String?>
}

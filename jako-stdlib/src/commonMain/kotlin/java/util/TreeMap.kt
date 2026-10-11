@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

import java.io.Serializable
import java.lang.Cloneable

expect open class TreeMap<K, V> : AbstractMutableMap<K, V>, Cloneable, Serializable {
    constructor()
    constructor(comparator: Comparator<in K>?)
    constructor(m: Map<out K, V>)

    fun comparator(): Comparator<in K>?
    fun firstKey(): K
    fun lastKey(): K

    override fun put(key: K, value: V): V?
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
}

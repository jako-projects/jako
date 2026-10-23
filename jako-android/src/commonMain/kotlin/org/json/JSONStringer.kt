@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package org.json

/**
 * [Nullability Decision] JSONStringer.toString() returns String? (null if no elements written yet in AOSP).
 */
expect open class JSONStringer {
    constructor()

    open fun array(): JSONStringer
    open fun endArray(): JSONStringer
    open fun `object`(): JSONStringer
    open fun endObject(): JSONStringer
    open fun value(value: Any?): JSONStringer
    open fun value(value: Boolean): JSONStringer
    open fun value(value: Double): JSONStringer
    open fun value(value: Long): JSONStringer
    open fun key(name: String): JSONStringer
    override fun toString(): String
}

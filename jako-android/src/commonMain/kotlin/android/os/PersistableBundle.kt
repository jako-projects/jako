@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

import java.lang.Cloneable

/**
 * A mapping from String keys to values of various types that can be saved to persistent and later
 * restored.
 */
expect final class PersistableBundle : BaseBundle, Cloneable, Parcelable {
    constructor()
    constructor(capacity: Int)
    constructor(b: PersistableBundle?)

    fun putPersistableBundle(key: String?, value: PersistableBundle?)
    fun getPersistableBundle(key: String?): PersistableBundle?
    fun clone(): Any
    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    override fun toString(): String
}

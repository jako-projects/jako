@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.graphics

import android.os.Parcel
import android.os.Parcelable

/**
 * Point holds two integer coordinates.
 */
expect open class Point : Parcelable {
    var x: Int
    var y: Int

    constructor()
    constructor(x: Int, y: Int)
    constructor(src: Point)

    open fun set(x: Int, y: Int)
    fun negate()
    fun offset(dx: Int, dy: Int)
    fun equals(x: Int, y: Int): Boolean
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    open fun readFromParcel(`in`: Parcel)

    companion object {
        val CREATOR: Parcelable.Creator<Point>
    }
}

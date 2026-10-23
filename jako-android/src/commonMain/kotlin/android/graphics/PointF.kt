@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.graphics

import android.os.Parcel
import android.os.Parcelable

/**
 * PointF holds two float coordinates.
 */
expect open class PointF : Parcelable {
    var x: Float
    var y: Float

    constructor()
    constructor(x: Float, y: Float)
    constructor(p: Point)

    fun set(x: Float, y: Float)
    fun set(p: PointF)
    fun negate()
    fun offset(dx: Float, dy: Float)
    fun equals(x: Float, y: Float): Boolean
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
    fun length(): Float
    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    open fun readFromParcel(`in`: Parcel)

    companion object {
        val CREATOR: Parcelable.Creator<PointF>
        fun length(x: Float, y: Float): Float
    }
}

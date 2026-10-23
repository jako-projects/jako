@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.graphics

import android.os.Parcel
import android.os.Parcelable

/**
 * RectF holds four float coordinates for a rectangle.
 */
expect open class RectF : Parcelable {
    var left: Float
    var top: Float
    var right: Float
    var bottom: Float
    val isEmpty: Boolean

    constructor()
    constructor(left: Float, top: Float, right: Float, bottom: Float)
    constructor(r: RectF?)
    constructor(r: Rect?)

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
    fun toShortString(): String

    fun isEmpty(): Boolean
    fun width(): Float
    fun height(): Float
    fun centerX(): Float
    fun centerY(): Float
    fun setEmpty()
    open fun set(left: Float, top: Float, right: Float, bottom: Float)
    open fun set(src: RectF)
    open fun set(src: Rect)
    fun offset(dx: Float, dy: Float)
    fun offsetTo(newLeft: Float, newTop: Float)
    fun inset(dx: Float, dy: Float)
    fun contains(x: Float, y: Float): Boolean
    fun contains(left: Float, top: Float, right: Float, bottom: Float): Boolean
    fun contains(r: RectF): Boolean
    fun intersect(left: Float, top: Float, right: Float, bottom: Float): Boolean
    fun intersect(r: RectF): Boolean
    fun setIntersect(a: RectF, b: RectF): Boolean
    fun intersects(left: Float, top: Float, right: Float, bottom: Float): Boolean
    fun round(dst: Rect)
    fun roundOut(dst: Rect)
    fun union(left: Float, top: Float, right: Float, bottom: Float)
    fun union(r: RectF)
    fun union(x: Float, y: Float)
    fun sort()

    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    open fun readFromParcel(`in`: Parcel)

    companion object {
        val CREATOR: Parcelable.Creator<RectF>
        fun intersects(a: RectF, b: RectF): Boolean
    }
}

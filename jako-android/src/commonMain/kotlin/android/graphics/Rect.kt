@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.graphics

import android.os.Parcel
import android.os.Parcelable

/**
 * Rect holds four integer coordinates for a rectangle.
 */
expect final class Rect : Parcelable {
    var left: Int
    var top: Int
    var right: Int
    var bottom: Int
    val isEmpty: Boolean

    constructor()
    constructor(left: Int, top: Int, right: Int, bottom: Int)
    constructor(r: Rect?)

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
    fun toShortString(): String
    fun flattenToString(): String

    fun isEmpty(): Boolean
    fun width(): Int
    fun height(): Int
    fun centerX(): Int
    fun centerY(): Int
    fun exactCenterX(): Float
    fun exactCenterY(): Float
    fun setEmpty()
    fun set(left: Int, top: Int, right: Int, bottom: Int)
    fun set(src: Rect)
    fun offset(dx: Int, dy: Int)
    fun offsetTo(newLeft: Int, newTop: Int)
    fun inset(dx: Int, dy: Int)
    fun contains(x: Int, y: Int): Boolean
    fun contains(left: Int, top: Int, right: Int, bottom: Int): Boolean
    fun contains(r: Rect): Boolean
    fun intersect(left: Int, top: Int, right: Int, bottom: Int): Boolean
    fun intersect(r: Rect): Boolean
    fun setIntersect(a: Rect, b: Rect): Boolean
    fun intersects(left: Int, top: Int, right: Int, bottom: Int): Boolean
    fun union(left: Int, top: Int, right: Int, bottom: Int)
    fun union(r: Rect)
    fun union(x: Int, y: Int)
    fun sort()

    override fun describeContents(): Int
    override fun writeToParcel(dest: Parcel, flags: Int)
    fun readFromParcel(`in`: Parcel)

    companion object {
        val CREATOR: Parcelable.Creator<Rect>
        fun intersects(a: Rect, b: Rect): Boolean
        fun unflattenFromString(str: String?): Rect?
    }
}

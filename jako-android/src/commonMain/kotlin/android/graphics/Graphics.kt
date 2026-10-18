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
}

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
        fun length(x: Float, y: Float): Float
    }
}

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
        fun intersects(a: Rect, b: Rect): Boolean
        fun unflattenFromString(str: String?): Rect?
    }
}

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
        fun intersects(a: RectF, b: RectF): Boolean
    }
}

/**
 * The Matrix class holds a 3x3 matrix for transforming coordinates.
 */
expect open class Matrix {
    val isIdentity: Boolean
    val isAffine: Boolean

    constructor()
    constructor(src: Matrix?)

    open fun isIdentity(): Boolean
    open fun isAffine(): Boolean
    open fun rectStaysRect(): Boolean
    open fun set(src: Matrix?)
    open fun reset()

    open fun setTranslate(dx: Float, dy: Float)
    open fun setScale(sx: Float, sy: Float, px: Float, py: Float)
    open fun setScale(sx: Float, sy: Float)
    open fun setRotate(degrees: Float, px: Float, py: Float)
    open fun setRotate(degrees: Float)
    open fun setSinCos(sinValue: Float, cosValue: Float, px: Float, py: Float)
    open fun setSinCos(sinValue: Float, cosValue: Float)
    open fun setSkew(kx: Float, ky: Float, px: Float, py: Float)
    open fun setSkew(kx: Float, ky: Float)
    open fun setConcat(a: Matrix, b: Matrix): Boolean

    open fun preTranslate(dx: Float, dy: Float): Boolean
    open fun preScale(sx: Float, sy: Float, px: Float, py: Float): Boolean
    open fun preScale(sx: Float, sy: Float): Boolean
    open fun preRotate(degrees: Float, px: Float, py: Float): Boolean
    open fun preRotate(degrees: Float): Boolean
    open fun preSkew(kx: Float, ky: Float, px: Float, py: Float): Boolean
    open fun preSkew(kx: Float, ky: Float): Boolean
    open fun preConcat(other: Matrix): Boolean

    open fun postTranslate(dx: Float, dy: Float): Boolean
    open fun postScale(sx: Float, sy: Float, px: Float, py: Float): Boolean
    open fun postScale(sx: Float, sy: Float): Boolean
    open fun postRotate(degrees: Float, px: Float, py: Float): Boolean
    open fun postRotate(degrees: Float): Boolean
    open fun postSkew(kx: Float, ky: Float, px: Float, py: Float): Boolean
    open fun postSkew(kx: Float, ky: Float): Boolean
    open fun postConcat(other: Matrix): Boolean

    open fun setRectToRect(src: RectF, dst: RectF, stf: ScaleToFit): Boolean
    open fun invert(inverse: Matrix?): Boolean

    open fun mapPoints(dst: FloatArray, dstIndex: Int, src: FloatArray, srcIndex: Int, pointCount: Int)
    open fun mapVectors(dst: FloatArray, dstIndex: Int, src: FloatArray, srcIndex: Int, vectorCount: Int)
    open fun mapPoints(dst: FloatArray, src: FloatArray)
    open fun mapVectors(dst: FloatArray, src: FloatArray)
    open fun mapPoints(pts: FloatArray)
    open fun mapVectors(vecs: FloatArray)
    open fun mapRect(dst: RectF, src: RectF): Boolean
    open fun mapRect(rect: RectF): Boolean
    open fun mapRadius(radius: Float): Float

    open fun getValues(values: FloatArray)
    open fun setValues(values: FloatArray)

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String
    open fun toShortString(): String

    enum class ScaleToFit {
        FILL,
        START,
        CENTER,
        END
    }

    companion object {
        val MSCALE_X: Int
        val MSKEW_X: Int
        val MTRANS_X: Int
        val MSKEW_Y: Int
        val MSCALE_Y: Int
        val MTRANS_Y: Int
        val MPERSP_0: Int
        val MPERSP_1: Int
        val MPERSP_2: Int
    }
}

/**
 * The Color class defines methods for creating, converting and manipulating colors.
 */
expect open class Color() {
    companion object {
        val BLACK: Int
        val DKGRAY: Int
        val GRAY: Int
        val LTGRAY: Int
        val WHITE: Int
        val RED: Int
        val GREEN: Int
        val BLUE: Int
        val YELLOW: Int
        val CYAN: Int
        val MAGENTA: Int
        val TRANSPARENT: Int

        fun alpha(color: Int): Int
        fun red(color: Int): Int
        fun green(color: Int): Int
        fun blue(color: Int): Int
        fun rgb(red: Int, green: Int, blue: Int): Int
        fun rgb(red: Float, green: Float, blue: Float): Int
        fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int
        fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int
        fun parseColor(colorString: String): Int
        fun RGBToHSV(red: Int, green: Int, blue: Int, hsv: FloatArray)
        fun colorToHSV(color: Int, hsv: FloatArray)
        fun HSVToColor(hsv: FloatArray): Int
        fun HSVToColor(alpha: Int, hsv: FloatArray): Int
    }
}

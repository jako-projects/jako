@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.graphics

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

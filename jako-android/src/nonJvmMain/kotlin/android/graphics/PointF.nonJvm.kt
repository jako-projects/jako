package android.graphics

import android.os.Parcel
import android.os.Parcelable
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

actual open class PointF : Parcelable {
    actual var x: Float = 0f
    actual var y: Float = 0f

    actual constructor()

    actual constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    actual constructor(p: Point) {
        this.x = p.x.toFloat()
        this.y = p.y.toFloat()
    }

    actual final fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    actual final fun set(p: PointF) {
        this.x = p.x
        this.y = p.y
    }

    actual final fun negate() {
        x = -x
        y = -y
    }

    actual final fun offset(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    actual final fun equals(x: Float, y: Float): Boolean = this.x == x && this.y == y

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as PointF
        return other.x.compareTo(x) == 0 && other.y.compareTo(y) == 0
    }

    actual override fun hashCode(): Int {
        var result = if (x != 0.0f) x.toBits() else 0
        result = 31 * result + if (y != 0.0f) y.toBits() else 0
        return result
    }

    actual override fun toString(): String = "PointF($x, $y)"

    actual final fun length(): Float = length(x, y)

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(x)
        dest.writeFloat(y)
    }

    actual open fun readFromParcel(`in`: Parcel) {
        x = `in`.readFloat()
        y = `in`.readFloat()
    }

    actual companion object {
        actual val CREATOR: Parcelable.Creator<PointF> = object : Parcelable.Creator<PointF> {
            override fun createFromParcel(source: Parcel): PointF {
                val p = PointF()
                p.readFromParcel(source)
                return p
            }

            override fun newArray(size: Int): Array<PointF?> = arrayOfNulls(size)
        }

        actual fun length(x: Float, y: Float): Float = hypot(x.toDouble(), y.toDouble()).toFloat()
    }
}

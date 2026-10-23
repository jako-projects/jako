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

actual open class Point : Parcelable {
    actual var x: Int = 0
    actual var y: Int = 0

    actual constructor()

    actual constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    actual constructor(src: Point) {
        this.x = src.x
        this.y = src.y
    }

    actual open fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    actual final fun negate() {
        x = -x
        y = -y
    }

    actual final fun offset(dx: Int, dy: Int) {
        x += dx
        y += dy
    }

    actual final fun equals(x: Int, y: Int): Boolean = this.x == x && this.y == y

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Point
        return x == other.x && y == other.y
    }

    actual override fun hashCode(): Int = 31 * x + y

    actual override fun toString(): String = "Point($x, $y)"

    actual override fun describeContents(): Int = 0

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(x)
        dest.writeInt(y)
    }

    actual open fun readFromParcel(`in`: Parcel) {
        x = `in`.readInt()
        y = `in`.readInt()
    }

    actual companion object {
        actual val CREATOR: Parcelable.Creator<Point> = object : Parcelable.Creator<Point> {
            override fun createFromParcel(source: Parcel): Point {
                val p = Point()
                p.readFromParcel(source)
                return p
            }

            override fun newArray(size: Int): Array<Point?> = arrayOfNulls(size)
        }
    }
}

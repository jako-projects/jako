@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Container for a message (data and object references) that can be sent through an IBinder
 * or serialized in memory.
 */
expect final class Parcel {
    fun recycle()
    fun dataPosition(): Int
    fun setDataPosition(pos: Int)
    fun dataSize(): Int
    fun writeByte(valByte: Byte)
    fun readByte(): Byte
    fun writeInt(valInt: Int)
    fun readInt(): Int
    fun writeLong(valLong: Long)
    fun readLong(): Long
    fun writeFloat(valFloat: Float)
    fun readFloat(): Float
    fun writeDouble(valDouble: Double)
    fun readDouble(): Double
    fun writeString(valStr: String?)
    fun readString(): String?
    fun writeByteArray(b: ByteArray?)
    fun createByteArray(): ByteArray?
    fun writeBooleanArray(valArray: BooleanArray?)
    fun createBooleanArray(): BooleanArray?
    fun writeCharArray(valArray: CharArray?)
    fun createCharArray(): CharArray?
    fun writeIntArray(valArray: IntArray?)
    fun createIntArray(): IntArray?
    fun writeLongArray(valArray: LongArray?)
    fun createLongArray(): LongArray?
    fun writeFloatArray(valArray: FloatArray?)
    fun createFloatArray(): FloatArray?
    fun writeDoubleArray(valArray: DoubleArray?)
    fun createDoubleArray(): DoubleArray?
    fun writeStringArray(valArray: Array<String?>?)
    fun createStringArray(): Array<String?>?
    fun writeStringList(valList: List<String>?)
    fun createStringArrayList(): ArrayList<String>?
    fun <T : Parcelable?> writeTypedObject(valObj: T, parcelableFlags: Int)
    fun <T> readTypedObject(c: Parcelable.Creator<T>): T?
    fun <T : Parcelable?> writeTypedList(valList: List<T>?)
    fun <T> createTypedArrayList(c: Parcelable.Creator<T>): ArrayList<T>?
    fun writeBundle(valBundle: Bundle?)
    fun readBundle(): Bundle?
    fun marshall(): ByteArray
    fun unmarshall(data: ByteArray, offset: Int, length: Int)

    companion object {
        fun obtain(): Parcel
    }
}

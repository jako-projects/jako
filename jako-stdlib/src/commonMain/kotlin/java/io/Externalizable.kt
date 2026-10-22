@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect interface Externalizable : Serializable {
    fun writeExternal(out: ObjectOutput)
    fun readExternal(inStream: ObjectInput)
}

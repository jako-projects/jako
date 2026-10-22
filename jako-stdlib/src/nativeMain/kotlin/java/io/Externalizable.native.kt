package java.io

actual interface Externalizable : Serializable {
    actual fun writeExternal(out: ObjectOutput)
    actual fun readExternal(inStream: ObjectInput)
}

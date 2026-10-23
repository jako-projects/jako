package java.io

actual interface ObjectStreamConstants {
    actual companion object {
        actual val STREAM_MAGIC: Short = 0xaced.toShort()
        actual val STREAM_VERSION: Short = 5
        actual val TC_BASE: Byte = 0x70
        actual val TC_NULL: Byte = 0x70
        actual val TC_REFERENCE: Byte = 0x71
        actual val TC_CLASSDESC: Byte = 0x72
        actual val TC_OBJECT: Byte = 0x73
        actual val TC_STRING: Byte = 0x74
        actual val TC_ARRAY: Byte = 0x75
        actual val TC_CLASS: Byte = 0x76
        actual val TC_BLOCKDATA: Byte = 0x77
        actual val TC_ENDBLOCKDATA: Byte = 0x78
        actual val TC_RESET: Byte = 0x79
        actual val TC_BLOCKDATALONG: Byte = 0x7A
        actual val TC_EXCEPTION: Byte = 0x7B
        actual val TC_LONGSTRING: Byte = 0x7C
        actual val TC_PROXYCLASSDESC: Byte = 0x7D
        actual val TC_ENUM: Byte = 0x7E
        actual val TC_MAX: Byte = 0x7E
        actual val baseWireHandle: Int = 0x7e0000
        actual val SC_WRITE_METHOD: Byte = 0x01
        actual val SC_BLOCK_DATA: Byte = 0x08
        actual val SC_SERIALIZABLE: Byte = 0x02
        actual val SC_EXTERNALIZABLE: Byte = 0x04
        actual val SC_ENUM: Byte = 0x10
        actual val PROTOCOL_VERSION_1: Int = 1
        actual val PROTOCOL_VERSION_2: Int = 2
    }
}

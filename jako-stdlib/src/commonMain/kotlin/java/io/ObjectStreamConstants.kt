@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect interface ObjectStreamConstants {
    companion object {
        val STREAM_MAGIC: Short
        val STREAM_VERSION: Short
        val TC_BASE: Byte
        val TC_NULL: Byte
        val TC_REFERENCE: Byte
        val TC_CLASSDESC: Byte
        val TC_OBJECT: Byte
        val TC_STRING: Byte
        val TC_ARRAY: Byte
        val TC_CLASS: Byte
        val TC_BLOCKDATA: Byte
        val TC_ENDBLOCKDATA: Byte
        val TC_RESET: Byte
        val TC_BLOCKDATALONG: Byte
        val TC_EXCEPTION: Byte
        val TC_LONGSTRING: Byte
        val TC_PROXYCLASSDESC: Byte
        val TC_ENUM: Byte
        val TC_MAX: Byte
        val baseWireHandle: Int
        val SC_WRITE_METHOD: Byte
        val SC_BLOCK_DATA: Byte
        val SC_SERIALIZABLE: Byte
        val SC_EXTERNALIZABLE: Byte
        val SC_ENUM: Byte
        val PROTOCOL_VERSION_1: Int
        val PROTOCOL_VERSION_2: Int
    }
}

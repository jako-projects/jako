package java.util.zip

import java.io.EOFException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.Calendar

internal const val LOCSIG = 0x04034b50L
internal const val EXTSIG = 0x08074b50L
internal const val CENSIG = 0x02014b50L
internal const val ENDSIG = 0x06054b50L

internal const val LOCHDR = 30
internal const val EXTHDR = 16
internal const val CENHDR = 46
internal const val ENDHDR = 22

internal const val GPBF_DATA_DESCRIPTOR = 8
internal const val GPBF_EFS = 0x800

internal fun get16(b: ByteArray, off: Int): Int {
    return (b[off].toInt() and 0xFF) or ((b[off + 1].toInt() and 0xFF) shl 8)
}

internal fun get32(b: ByteArray, off: Int): Long {
    return (b[off].toLong() and 0xFF) or
        ((b[off + 1].toLong() and 0xFF) shl 8) or
        ((b[off + 2].toLong() and 0xFF) shl 16) or
        ((b[off + 3].toLong() and 0xFF) shl 24)
}

internal fun InputStream.readFully(buf: ByteArray, off: Int, len: Int) {
    var n = 0
    while (n < len) {
        val count = read(buf, off + n, len - n)
        if (count < 0) throw EOFException()
        n += count
    }
}

internal fun RandomAccessFile.readFully(buf: ByteArray, off: Int = 0, len: Int = buf.size) {
    var n = 0
    while (n < len) {
        val count = read(buf, off + n, len - n)
        if (count < 0) throw EOFException()
        n += count
    }
}

internal fun javaTimeToDosTime(time: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = time
    val year = cal.get(Calendar.YEAR)
    if (year < 1980) {
        return (1 shl 21) or (1 shl 16)
    }
    return ((year - 1980) shl 25) or
        ((cal.get(Calendar.MONTH) + 1) shl 21) or
        (cal.get(Calendar.DAY_OF_MONTH) shl 16) or
        (cal.get(Calendar.HOUR_OF_DAY) shl 11) or
        (cal.get(Calendar.MINUTE) shl 5) or
        (cal.get(Calendar.SECOND) shr 1)
}

internal fun dosTimeToJavaTime(dostime: Int): Long {
    val cal = Calendar.getInstance()
    cal.set(
        ((dostime ushr 25) and 0x7F) + 1980,
        ((dostime ushr 21) and 0x0F) - 1,
        (dostime ushr 16) and 0x1F,
        (dostime ushr 11) and 0x1F,
        (dostime ushr 5) and 0x3F,
        (dostime shl 1) and 0x3E
    )
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

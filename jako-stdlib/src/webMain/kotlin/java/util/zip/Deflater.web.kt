package java.util.zip

internal val EMPTY_BYTES = ByteArray(0)

actual open class Deflater {
    private val nowrap: Boolean
    private var level: Int
    private var strategy: Int = DEFAULT_STRATEGY
    private var input: ByteArray = EMPTY_BYTES
    private var inputOff = 0
    private var inputLen = 0
    private var finishRequested = false
    private var finalBlockEmitted = false
    private var finishedFlag = false
    private var ended = false
    private var headerEmitted = false

    private val adler32 = Adler32()
    private var dictAdler = 1
    private var dictAdlerActive = false
    private var hasDict = false
    private var history = EMPTY_BYTES
    private var totalRead = 0L
    private var totalWritten = 0L

    private var outQueue = ByteArray(1024)
    private var outHead = 0
    private var outTail = 0

    private val pendingUncompressed = java.io.ByteArrayOutputStream()

    actual constructor() : this(DEFAULT_COMPRESSION, false)

    actual constructor(level: Int) : this(level, false)

    actual constructor(level: Int, nowrap: Boolean) {
        if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw IllegalArgumentException("invalid compression level")
        }
        this.level = level
        this.nowrap = nowrap
    }

    actual fun setInput(b: ByteArray) {
        setInput(b, 0, b.size)
    }

    actual fun setInput(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        input = b
        inputOff = off
        inputLen = len
    }

    actual fun setDictionary(b: ByteArray) {
        setDictionary(b, 0, b.size)
    }

    actual fun setDictionary(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        val dAdler = Adler32()
        dAdler.update(b, off, len)
        dictAdler = dAdler.getValue().toInt()
        dictAdlerActive = true
        if (!headerEmitted) {
            hasDict = true
        }
        val windowLen = minOf(len, 32768)
        history = b.copyOfRange(off + len - windowLen, off + len)
    }

    actual fun setStrategy(strategy: Int) {
        when (strategy) {
            DEFAULT_STRATEGY, FILTERED, HUFFMAN_ONLY -> this.strategy = strategy
            else -> throw IllegalArgumentException("invalid compression strategy")
        }
    }

    actual fun setLevel(level: Int) {
        if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw IllegalArgumentException("invalid compression level")
        }
        this.level = level
    }

    actual fun needsInput(): Boolean = inputLen <= 0 && outHead >= outTail

    actual fun finish() {
        finishRequested = true
    }

    actual fun finished(): Boolean = finishedFlag

    actual fun deflate(b: ByteArray): Int = deflate(b, 0, b.size)

    actual fun deflate(b: ByteArray, off: Int, len: Int): Int = deflate(b, off, len, NO_FLUSH)

    actual fun deflate(b: ByteArray, off: Int, len: Int, flush: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        when (flush) {
            NO_FLUSH, SYNC_FLUSH, FULL_FLUSH -> {}
            else -> throw IllegalArgumentException("invalid flush mode")
        }

        if (inputLen > 0) {
            dictAdlerActive = false
            adler32.update(input, inputOff, inputLen)
            pendingUncompressed.write(input, inputOff, inputLen)
            totalRead += inputLen
            inputOff += inputLen
            inputLen = 0
        }

        var totalCopied = 0
        while (totalCopied < len && !finishedFlag) {
            if (outHead >= outTail) {
                if (finishRequested && !finalBlockEmitted) {
                    emitDeflateBlock(finalBlock = true, clearHistory = true)
                    finalBlockEmitted = true
                } else if (!finishRequested && (flush == SYNC_FLUSH || flush == FULL_FLUSH) && (pendingUncompressed.size() > 0 || (!nowrap && !headerEmitted))) {
                    emitDeflateBlock(finalBlock = false, clearHistory = (flush == FULL_FLUSH))
                } else if (!finishRequested && pendingUncompressed.size() >= 32768) {
                    emitDeflateBlock(finalBlock = false, clearHistory = false)
                } else {
                    break
                }
            }

            val avail = outTail - outHead
            if (avail <= 0) break
            val toCopy = minOf(len - totalCopied, avail)
            outQueue.copyInto(b, off + totalCopied, outHead, outHead + toCopy)
            outHead += toCopy
            totalCopied += toCopy
            totalWritten += toCopy
            if (outHead >= outTail) {
                outHead = 0
                outTail = 0
                if (finalBlockEmitted) {
                    finishedFlag = true
                }
            }
        }
        return totalCopied
    }

    private fun enqueueByte(v: Int) {
        if (outTail >= outQueue.size) {
            outQueue = outQueue.copyOf(outQueue.size * 2)
        }
        outQueue[outTail++] = v.toByte()
    }

    private fun emitDeflateBlock(finalBlock: Boolean, clearHistory: Boolean) {
        if (!nowrap && !headerEmitted) {
            if (hasDict) {
                enqueueByte(0x78)
                enqueueByte(0xBB)
                enqueueByte((dictAdler ushr 24) and 0xFF)
                enqueueByte((dictAdler ushr 16) and 0xFF)
                enqueueByte((dictAdler ushr 8) and 0xFF)
                enqueueByte(dictAdler and 0xFF)
            } else {
                enqueueByte(0x78)
                enqueueByte(0x9C)
            }
            headerEmitted = true
            dictAdlerActive = false
        }
        val raw = pendingUncompressed.toByteArray()
        pendingUncompressed.reset()

        if (level == NO_COMPRESSION) {
            var pos = 0
            if (raw.isEmpty()) {
                enqueueByte(if (finalBlock) 1 else 0)
                enqueueByte(0)
                enqueueByte(0)
                enqueueByte(0xFF)
                enqueueByte(0xFF)
            } else {
                while (pos < raw.size) {
                    val chunk = minOf(65535, raw.size - pos)
                    val isLast = finalBlock && (pos + chunk == raw.size)
                    enqueueByte(if (isLast) 1 else 0)
                    enqueueByte(chunk and 0xFF)
                    enqueueByte((chunk ushr 8) and 0xFF)
                    val nlen = chunk.inv() and 0xFFFF
                    enqueueByte(nlen and 0xFF)
                    enqueueByte((nlen ushr 8) and 0xFF)
                    for (i in 0 until chunk) {
                        enqueueByte(raw[pos + i].toInt() and 0xFF)
                    }
                    pos += chunk
                }
            }
            updateHistoryWindow(raw, clearHistory)
        } else {
            val bw = BitWriter()
            if (finalBlock || raw.isNotEmpty()) {
                bw.writeBits(if (finalBlock) 1 else 0, 1)
                bw.writeBits(1, 2) // BTYPE = 01 (Fixed Huffman)
                encodeLz77FixedHuffman(raw, bw, strategy == HUFFMAN_ONLY, clearHistory)
                bw.writeFixedLiteralCode(256) // End of block
            } else if (clearHistory) {
                history = EMPTY_BYTES
            }
            if (!finalBlock) {
                // RFC 1951 Z_SYNC_FLUSH: align bitstream to byte boundary using an empty stored block
                bw.writeBits(0, 1) // BFINAL = 0
                bw.writeBits(0, 2) // BTYPE = 00 (Stored block)
                bw.flushAlignToByte()
            } else {
                bw.flushAlignToByte()
            }
            val encoded = bw.toByteArray()
            for (b in encoded) {
                enqueueByte(b.toInt() and 0xFF)
            }
            if (!finalBlock) {
                enqueueByte(0x00)
                enqueueByte(0x00)
                enqueueByte(0xFF)
                enqueueByte(0xFF)
            }
        }

        if (finalBlock && !nowrap) {
            val a = adler32.getValue().toInt()
            enqueueByte((a ushr 24) and 0xFF)
            enqueueByte((a ushr 16) and 0xFF)
            enqueueByte((a ushr 8) and 0xFF)
            enqueueByte(a and 0xFF)
        }
    }

    private fun updateHistoryWindow(raw: ByteArray, clearHistory: Boolean) {
        if (clearHistory) {
            history = EMPTY_BYTES
            return
        }
        if (raw.isEmpty()) return
        val combined = if (history.isEmpty()) raw else history + raw
        val windowLen = minOf(32768, combined.size)
        history = combined.copyOfRange(combined.size - windowLen, combined.size)
    }

    private fun encodeLz77FixedHuffman(data: ByteArray, bw: BitWriter, huffmanOnly: Boolean, clearHistory: Boolean) {
        if (data.isEmpty()) {
            if (clearHistory) history = EMPTY_BYTES
            return
        }
        val histLen = history.size
        if (huffmanOnly || (histLen + data.size) < 4) {
            for (b in data) {
                bw.writeFixedLiteralCode(b.toInt() and 0xFF)
            }
            updateHistoryWindow(data, clearHistory)
            return
        }
        val window = if (histLen == 0) data else history + data
        val head = IntArray(4096) { -1 }
        val prev = IntArray(window.size) { -1 }
        for (pos in 0 until histLen) {
            if (pos + 3 <= window.size) {
                val h = (((window[pos].toInt() and 0xFF) shl 10) xor
                    ((window[pos + 1].toInt() and 0xFF) shl 5) xor
                    (window[pos + 2].toInt() and 0xFF)) and 0xFFF
                prev[pos] = head[h]
                head[h] = pos
            }
        }
        var i = histLen
        while (i < window.size) {
            var bestLen = 0
            var bestDist = 0
            if (i + 3 <= window.size) {
                val h = (((window[i].toInt() and 0xFF) shl 10) xor
                    ((window[i + 1].toInt() and 0xFF) shl 5) xor
                    (window[i + 2].toInt() and 0xFF)) and 0xFFF
                var matchPos = head[h]
                prev[i] = matchPos
                head[h] = i
                var chain = 0
                val maxChain = if (level == BEST_SPEED) 8 else 32
                while (matchPos >= 0 && i - matchPos <= 32768 && chain < maxChain) {
                    chain++
                    val maxLen = minOf(258, window.size - i)
                    var l = 0
                    while (l < maxLen && window[matchPos + l] == window[i + l]) {
                        l++
                    }
                    if (l >= 3 && l > bestLen) {
                        bestLen = l
                        bestDist = i - matchPos
                        if (l == 258) break
                    }
                    matchPos = prev[matchPos]
                }
            }
            if (bestLen >= 3) {
                bw.writeLengthDistance(bestLen, bestDist)
                val end = i + bestLen
                i++
                while (i < end) {
                    if (i + 3 <= window.size) {
                        val h = (((window[i].toInt() and 0xFF) shl 10) xor
                            ((window[i + 1].toInt() and 0xFF) shl 5) xor
                            (window[i + 2].toInt() and 0xFF)) and 0xFFF
                        prev[i] = head[h]
                        head[h] = i
                    }
                    i++
                }
            } else {
                bw.writeFixedLiteralCode(window[i].toInt() and 0xFF)
                i++
            }
        }
        if (clearHistory) {
            history = EMPTY_BYTES
        } else {
            val windowLen = minOf(32768, window.size)
            history = window.copyOfRange(window.size - windowLen, window.size)
        }
    }

    actual fun getAdler(): Int {
        ensureOpen()
        return if (dictAdlerActive) dictAdler else adler32.getValue().toInt()
    }
    actual val adler: Int get() = getAdler()
    actual fun getTotalIn(): Int = getBytesRead().toInt()
    actual val totalIn: Int get() = getTotalIn()
    actual fun getTotalOut(): Int = getBytesWritten().toInt()
    actual val totalOut: Int get() = getTotalOut()
    actual fun getBytesRead(): Long {
        ensureOpen()
        return totalRead
    }
    actual val bytesRead: Long get() = getBytesRead()
    actual fun getBytesWritten(): Long {
        ensureOpen()
        return totalWritten
    }
    actual val bytesWritten: Long get() = getBytesWritten()

    actual fun reset() {
        ensureOpen()
        input = EMPTY_BYTES
        inputOff = 0
        inputLen = 0
        finishRequested = false
        finalBlockEmitted = false
        finishedFlag = false
        headerEmitted = false
        adler32.reset()
        dictAdler = 1
        dictAdlerActive = false
        hasDict = false
        history = EMPTY_BYTES
        totalRead = 0L
        totalWritten = 0L
        outHead = 0
        outTail = 0
        pendingUncompressed.reset()
    }

    actual fun end() {
        if (ended) return
        ended = true
        input = EMPTY_BYTES
        history = EMPTY_BYTES
        pendingUncompressed.reset()
    }

    private fun ensureOpen() {
        if (ended) throw NullPointerException("Deflater has been closed")
    }

    actual companion object {
        actual val DEFLATED: Int = 8
        actual val NO_COMPRESSION: Int = 0
        actual val BEST_SPEED: Int = 1
        actual val BEST_COMPRESSION: Int = 9
        actual val DEFAULT_COMPRESSION: Int = -1
        actual val FILTERED: Int = 1
        actual val HUFFMAN_ONLY: Int = 2
        actual val DEFAULT_STRATEGY: Int = 0
        actual val NO_FLUSH: Int = 0
        actual val SYNC_FLUSH: Int = 2
        actual val FULL_FLUSH: Int = 3
    }
}

private class BitWriter {
    private var buf = ByteArray(256)
    private var size = 0
    private var bitBuf = 0
    private var bitCount = 0

    fun writeBits(value: Int, numBits: Int) {
        bitBuf = bitBuf or ((value and ((1 shl numBits) - 1)) shl bitCount)
        bitCount += numBits
        while (bitCount >= 8) {
            appendByte(bitBuf and 0xFF)
            bitBuf = bitBuf ushr 8
            bitCount -= 8
        }
    }

    private fun writeHuffmanBits(code: Int, numBits: Int) {
        var reversed = 0
        var c = code
        for (i in 0 until numBits) {
            reversed = (reversed shl 1) or (c and 1)
            c = c ushr 1
        }
        writeBits(reversed, numBits)
    }

    fun writeFixedLiteralCode(symbol: Int) {
        when (symbol) {
            in 0..143 -> writeHuffmanBits(0x30 + symbol, 8)
            in 144..255 -> writeHuffmanBits(0x190 + (symbol - 144), 9)
            in 256..279 -> writeHuffmanBits(symbol - 256, 7)
            in 280..287 -> writeHuffmanBits(0xC0 + (symbol - 280), 8)
        }
    }

    fun writeLengthDistance(length: Int, distance: Int) {
        var lenCode = 257
        for (i in LENGTH_BASE.indices) {
            val base = LENGTH_BASE[i]
            val extra = LENGTH_EXTRA[i]
            val max = base + (1 shl extra) - 1
            if (length in base..max || (i == 28 && length == 258)) {
                lenCode = 257 + i
                writeFixedLiteralCode(lenCode)
                if (extra > 0) {
                    writeBits(length - base, extra)
                }
                break
            }
        }
        for (i in DIST_BASE.indices) {
            val base = DIST_BASE[i]
            val extra = DIST_EXTRA[i]
            val max = base + (1 shl extra) - 1
            if (distance in base..max) {
                writeHuffmanBits(i, 5)
                if (extra > 0) {
                    writeBits(distance - base, extra)
                }
                break
            }
        }
    }

    fun flushAlignToByte() {
        if (bitCount > 0) {
            appendByte(bitBuf and 0xFF)
            bitBuf = 0
            bitCount = 0
        }
    }

    fun toByteArray(): ByteArray = buf.copyOf(size)

    private fun appendByte(b: Int) {
        if (size >= buf.size) {
            buf = buf.copyOf(buf.size * 2)
        }
        buf[size++] = b.toByte()
    }

    companion object {
        val LENGTH_BASE = intArrayOf(
            3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31,
            35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258
        )
        val LENGTH_EXTRA = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2,
            3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0
        )
        val DIST_BASE = intArrayOf(
            1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193,
            257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577
        )
        val DIST_EXTRA = intArrayOf(
            0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
            7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13
        )
    }
}

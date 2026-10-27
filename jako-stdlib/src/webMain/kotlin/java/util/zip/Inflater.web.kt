package java.util.zip

private class NeedMoreInputException : Exception()

actual open class Inflater {
    private val nowrap: Boolean
    private var inBuf = ByteArray(256)
    private var inSize = 0
    private var consumedBytes = 0
    private var bitOffset = 0 // 0..7 within inBuf[consumedBytes]

    private var outBuf = ByteArray(1024)
    private var outReadPos = 0
    private var outWritePos = 0

    private var headerParsed = false
    private var blocksDone = false
    private var finishedFlag = false
    private var needDict = false
    private var needMoreInput = true
    private var ended = false

    private var dictAdler = 1
    private var dictAdlerActive = false
    private var dictBuf = EMPTY_BYTES

    private var inBlock = false
    private var currentBfinal = 0
    private var currentBtype = -1
    private var remainingStoredBytes = -1
    private var activeLitTree: HuffmanTree? = null
    private var activeDistTree: HuffmanTree? = null

    private val adler32 = Adler32()
    private var totalWritten = 0L

    actual constructor() : this(false)

    actual constructor(nowrap: Boolean) {
        this.nowrap = nowrap
        this.headerParsed = nowrap
    }

    actual fun setInput(b: ByteArray) {
        setInput(b, 0, b.size)
    }

    actual fun setInput(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        if (inSize + len > inBuf.size) {
            inBuf = inBuf.copyOf(maxOf(inBuf.size * 2, inSize + len))
        }
        b.copyInto(inBuf, inSize, off, off + len)
        inSize += len
        needMoreInput = false
    }

    actual fun setDictionary(b: ByteArray) {
        setDictionary(b, 0, b.size)
    }

    actual fun setDictionary(b: ByteArray, off: Int, len: Int) {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (!nowrap && needDict) {
            val dAdler = Adler32()
            dAdler.update(b, off, len)
            if (dAdler.getValue().toInt() != dictAdler) {
                throw IllegalArgumentException("Incorrect dictionary")
            }
        }
        val windowLen = minOf(len, 32768)
        dictBuf = b.copyOfRange(off + len - windowLen, off + len)
        needDict = false
        dictAdlerActive = false
    }

    actual fun getRemaining(): Int {
        ensureOpen()
        val rem = inSize - consumedBytes - (if (bitOffset > 0) 1 else 0)
        return if (rem < 0) 0 else rem
    }
    actual val remaining: Int get() = getRemaining()

    actual fun needsInput(): Boolean {
        ensureOpen()
        if (outReadPos < outWritePos || finishedFlag || needDict) return false
        return needMoreInput || (consumedBytes >= inSize && bitOffset == 0)
    }

    actual fun needsDictionary(): Boolean = needDict

    actual fun finished(): Boolean = finishedFlag && outReadPos >= outWritePos

    actual fun inflate(b: ByteArray): Int = inflate(b, 0, b.size)

    actual fun inflate(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        if (off < 0 || len < 0 || off > b.size - len) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0

        if (outReadPos >= outWritePos && !finishedFlag) {
            decodeAvailableBlocks()
        }

        val avail = outWritePos - outReadPos
        if (avail <= 0) return 0
        val toCopy = minOf(len, avail)
        outBuf.copyInto(b, off, outReadPos, outReadPos + toCopy)
        outReadPos += toCopy
        totalWritten += toCopy
        return toCopy
    }

    private fun decodeAvailableBlocks() {
        if (!headerParsed) {
            if (inSize - consumedBytes < 2) {
                needMoreInput = true
                return
            }
            val cmf = inBuf[consumedBytes].toInt() and 0xFF
            val flg = inBuf[consumedBytes + 1].toInt() and 0xFF
            if ((cmf and 0x0F) != 8 || (cmf ushr 4) > 7 || ((cmf * 256 + flg) % 31) != 0) {
                throw DataFormatException("incorrect header check")
            }
            val fdict = (flg and 0x20) != 0
            if (fdict) {
                if (inSize - consumedBytes < 6) {
                    needMoreInput = true
                    return
                }
                dictAdler = ((inBuf[consumedBytes + 2].toInt() and 0xFF) shl 24) or
                    ((inBuf[consumedBytes + 3].toInt() and 0xFF) shl 16) or
                    ((inBuf[consumedBytes + 4].toInt() and 0xFF) shl 8) or
                    (inBuf[consumedBytes + 5].toInt() and 0xFF)
                consumedBytes += 6
                needDict = true
                dictAdlerActive = true
                headerParsed = true
                return
            }
            consumedBytes += 2
            headerParsed = true
        }

        if (needDict) return

        while (!blocksDone) {
            if (!inBlock) {
                val savedConsumed = consumedBytes
                val savedBitOff = bitOffset
                try {
                    val bfinal = readBits(1)
                    val btype = readBits(2)
                    when (btype) {
                        0 -> {
                            alignToByte()
                            if (inSize - consumedBytes < 4) throw NeedMoreInputException()
                            val len = (inBuf[consumedBytes].toInt() and 0xFF) or
                                ((inBuf[consumedBytes + 1].toInt() and 0xFF) shl 8)
                            val nlen = (inBuf[consumedBytes + 2].toInt() and 0xFF) or
                                ((inBuf[consumedBytes + 3].toInt() and 0xFF) shl 8)
                            if ((len xor nlen) != 0xFFFF) {
                                throw DataFormatException("invalid stored block lengths")
                            }
                            consumedBytes += 4
                            currentBfinal = bfinal
                            currentBtype = 0
                            remainingStoredBytes = len
                            inBlock = true
                        }
                        1 -> {
                            currentBfinal = bfinal
                            currentBtype = 1
                            activeLitTree = FIXED_LIT_TREE
                            activeDistTree = FIXED_DIST_TREE
                            inBlock = true
                        }
                        2 -> {
                            val trees = decodeDynamicTrees()
                            currentBfinal = bfinal
                            currentBtype = 2
                            activeLitTree = trees.first
                            activeDistTree = trees.second
                            inBlock = true
                        }
                        else -> throw DataFormatException("invalid block type")
                    }
                } catch (_: NeedMoreInputException) {
                    consumedBytes = savedConsumed
                    bitOffset = savedBitOff
                    needMoreInput = true
                    return
                }
            }

            if (currentBtype == 0) {
                val avail = inSize - consumedBytes
                val toCopy = minOf(remainingStoredBytes, avail)
                if (toCopy > 0) {
                    ensureOutCapacity(toCopy)
                    inBuf.copyInto(outBuf, outWritePos, consumedBytes, consumedBytes + toCopy)
                    adler32.update(outBuf, outWritePos, toCopy)
                    outWritePos += toCopy
                    consumedBytes += toCopy
                    remainingStoredBytes -= toCopy
                }
                if (remainingStoredBytes > 0) {
                    needMoreInput = true
                    return
                }
                inBlock = false
                if (currentBfinal == 1) {
                    blocksDone = true
                }
            } else {
                val litTree = activeLitTree!!
                val distTree = activeDistTree!!
                while (true) {
                    val symConsumed = consumedBytes
                    val symBitOff = bitOffset
                    try {
                        val sym = litTree.decodeSymbol(this)
                        if (sym < 256) {
                            appendOutByte(sym)
                        } else if (sym == 256) {
                            inBlock = false
                            activeLitTree = null
                            activeDistTree = null
                            if (currentBfinal == 1) {
                                alignToByte()
                                blocksDone = true
                            }
                            break
                        } else if (sym in 257..285) {
                            val lenIdx = sym - 257
                            val length = LENGTH_BASE[lenIdx] + readBits(LENGTH_EXTRA[lenIdx])
                            val distSym = distTree.decodeSymbol(this)
                            if (distSym !in 0..29) throw DataFormatException("invalid distance code")
                            val distance = DIST_BASE[distSym] + readBits(DIST_EXTRA[distSym])
                            if (distance > outWritePos + dictBuf.size) {
                                throw DataFormatException("invalid distance too far back")
                            }
                            ensureOutCapacity(length)
                            val start = outWritePos
                            for (i in 0 until length) {
                                val srcPos = outWritePos - distance
                                outBuf[outWritePos] = if (srcPos >= 0) {
                                    outBuf[srcPos]
                                } else {
                                    dictBuf[dictBuf.size + srcPos]
                                }
                                outWritePos++
                            }
                            adler32.update(outBuf, start, length)
                        } else {
                            throw DataFormatException("invalid literal/length code")
                        }
                    } catch (_: NeedMoreInputException) {
                        consumedBytes = symConsumed
                        bitOffset = symBitOff
                        needMoreInput = true
                        return
                    }
                }
            }
        }

        if (blocksDone && !finishedFlag) {
            if (nowrap) {
                finishedFlag = true
            } else {
                if (inSize - consumedBytes < 4) {
                    needMoreInput = true
                    return
                }
                val expectedAdler = ((inBuf[consumedBytes].toInt() and 0xFF) shl 24) or
                    ((inBuf[consumedBytes + 1].toInt() and 0xFF) shl 16) or
                    ((inBuf[consumedBytes + 2].toInt() and 0xFF) shl 8) or
                    (inBuf[consumedBytes + 3].toInt() and 0xFF)
                consumedBytes += 4
                if (expectedAdler != adler32.getValue().toInt()) {
                    throw DataFormatException("incorrect data check")
                }
                finishedFlag = true
            }
        }
    }

    private fun decodeDynamicTrees(): Pair<HuffmanTree, HuffmanTree> {
        val hlit = readBits(5) + 257
        val hdist = readBits(5) + 1
        val hclen = readBits(4) + 4
        if (hlit > 286 || hdist > 30) {
            throw DataFormatException("too many length or distance symbols")
        }
        val codeLengths = IntArray(19)
        for (i in 0 until hclen) {
            codeLengths[CL_ORDER[i]] = readBits(3)
        }
        val codeLenTree = HuffmanTree.fromLengths(codeLengths)
        val totalCodes = hlit + hdist
        val lengths = IntArray(totalCodes)
        var idx = 0
        while (idx < totalCodes) {
            val sym = codeLenTree.decodeSymbol(this)
            when (sym) {
                in 0..15 -> lengths[idx++] = sym
                16 -> {
                    if (idx == 0) throw DataFormatException("invalid bit length repeat")
                    val prev = lengths[idx - 1]
                    val rep = 3 + readBits(2)
                    if (idx + rep > totalCodes) throw DataFormatException("invalid bit length repeat")
                    repeat(rep) { lengths[idx++] = prev }
                }
                17 -> {
                    val rep = 3 + readBits(3)
                    if (idx + rep > totalCodes) throw DataFormatException("invalid bit length repeat")
                    repeat(rep) { lengths[idx++] = 0 }
                }
                18 -> {
                    val rep = 11 + readBits(7)
                    if (idx + rep > totalCodes) throw DataFormatException("invalid bit length repeat")
                    repeat(rep) { lengths[idx++] = 0 }
                }
                else -> throw DataFormatException("invalid code lengths set")
            }
        }
        val litLengths = lengths.copyOfRange(0, hlit)
        val distLengths = lengths.copyOfRange(hlit, totalCodes)
        if (litLengths[256] == 0) {
            throw DataFormatException("missing end-of-block code")
        }
        return Pair(HuffmanTree.fromLengths(litLengths), HuffmanTree.fromLengths(distLengths))
    }

    internal fun readBit(): Int {
        if (consumedBytes >= inSize) throw NeedMoreInputException()
        val bit = ((inBuf[consumedBytes].toInt() and 0xFF) ushr bitOffset) and 1
        bitOffset++
        if (bitOffset == 8) {
            bitOffset = 0
            consumedBytes++
        }
        return bit
    }

    internal fun readBits(count: Int): Int {
        var res = 0
        for (i in 0 until count) {
            res = res or (readBit() shl i)
        }
        return res
    }

    private fun alignToByte() {
        if (bitOffset > 0) {
            bitOffset = 0
            consumedBytes++
        }
    }

    private fun ensureOutCapacity(extra: Int) {
        if (outWritePos + extra > outBuf.size) {
            outBuf = outBuf.copyOf(maxOf(outBuf.size * 2, outWritePos + extra))
        }
    }

    private fun appendOutByte(v: Int) {
        ensureOutCapacity(1)
        outBuf[outWritePos++] = v.toByte()
        adler32.update(v and 0xFF)
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
        return (consumedBytes + if (bitOffset > 0) 1 else 0).toLong()
    }
    actual val bytesRead: Long get() = getBytesRead()
    actual fun getBytesWritten(): Long {
        ensureOpen()
        return totalWritten
    }
    actual val bytesWritten: Long get() = getBytesWritten()

    actual fun reset() {
        ensureOpen()
        inSize = 0
        consumedBytes = 0
        bitOffset = 0
        outReadPos = 0
        outWritePos = 0
        headerParsed = nowrap
        blocksDone = false
        finishedFlag = false
        needDict = false
        needMoreInput = true
        dictAdler = 1
        dictAdlerActive = false
        dictBuf = EMPTY_BYTES
        inBlock = false
        currentBfinal = 0
        currentBtype = -1
        remainingStoredBytes = -1
        activeLitTree = null
        activeDistTree = null
        adler32.reset()
        totalWritten = 0L
    }

    actual fun end() {
        if (ended) return
        ended = true
        inBuf = EMPTY_BYTES
        outBuf = EMPTY_BYTES
        dictBuf = EMPTY_BYTES
        activeLitTree = null
        activeDistTree = null
    }

    private fun ensureOpen() {
        if (ended) throw NullPointerException("Inflater has been closed")
    }

    private class HuffmanTree(
        private val left: IntArray,
        private val right: IntArray,
        private val symbol: IntArray
    ) {
        fun decodeSymbol(inf: Inflater): Int {
            var node = 0
            while (true) {
                val bit = inf.readBit()
                node = if (bit == 0) left[node] else right[node]
                if (node < 0) throw DataFormatException("invalid Huffman code")
                val sym = symbol[node]
                if (sym >= 0) return sym
            }
        }

        companion object {
            fun fromLengths(lengths: IntArray): HuffmanTree {
                var maxBits = 0
                for (l in lengths) {
                    if (l > maxBits) maxBits = l
                }
                if (maxBits == 0) {
                    return HuffmanTree(IntArray(1) { -1 }, IntArray(1) { -1 }, IntArray(1) { -1 })
                }
                val blCount = IntArray(maxBits + 1)
                for (l in lengths) {
                    if (l > 0) blCount[l]++
                }
                val nextCode = IntArray(maxBits + 1)
                var code = 0
                for (bits in 1..maxBits) {
                    code = (code + blCount[bits - 1]) shl 1
                    nextCode[bits] = code
                }
                val maxNodes = lengths.size * 2 + 2
                val left = IntArray(maxNodes) { -1 }
                val right = IntArray(maxNodes) { -1 }
                val symbol = IntArray(maxNodes) { -1 }
                var nodeCount = 1

                for (sym in lengths.indices) {
                    val len = lengths[sym]
                    if (len > 0) {
                        val c = nextCode[len]++
                        var curr = 0
                        for (bitPos in len - 1 downTo 0) {
                            val bit = (c ushr bitPos) and 1
                            val next = if (bit == 0) left[curr] else right[curr]
                            if (next < 0) {
                                val created = nodeCount++
                                if (bit == 0) left[curr] = created else right[curr] = created
                                curr = created
                            } else {
                                curr = next
                            }
                        }
                        symbol[curr] = sym
                    }
                }
                return HuffmanTree(left, right, symbol)
            }
        }
    }

    companion object {
        private val CL_ORDER = intArrayOf(
            16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15
        )
        private val LENGTH_BASE = intArrayOf(
            3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31,
            35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258
        )
        private val LENGTH_EXTRA = intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2,
            3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0
        )
        private val DIST_BASE = intArrayOf(
            1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193,
            257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577
        )
        private val DIST_EXTRA = intArrayOf(
            0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
            7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13
        )

        private val FIXED_LIT_TREE: HuffmanTree by lazy {
            val lengths = IntArray(288)
            for (i in 0..143) lengths[i] = 8
            for (i in 144..255) lengths[i] = 9
            for (i in 256..279) lengths[i] = 7
            for (i in 280..287) lengths[i] = 8
            HuffmanTree.fromLengths(lengths)
        }

        private val FIXED_DIST_TREE: HuffmanTree by lazy {
            val lengths = IntArray(32) { 5 }
            HuffmanTree.fromLengths(lengths)
        }
    }
}

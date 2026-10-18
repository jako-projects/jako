package java.io

private const val CT_WHITESPACE: Byte = 1
private const val CT_DIGIT: Byte = 2
private const val CT_ALPHA: Byte = 4
private const val CT_QUOTE: Byte = 8
private const val CT_COMMENT: Byte = 16

private const val NEED_CHAR = Int.MAX_VALUE
private const val SKIP_LF = Int.MAX_VALUE - 1
private const val TT_NOTHING = -4

actual open class StreamTokenizer {
    private var buf = CharArray(20)
    private var peekc: Int = NEED_CHAR
    private var pushedBack: Boolean = false
    private var forceLower: Boolean = false
    private var lineNumber: Int = 1
    private var eolIsSignificantP: Boolean = false
    private var slashSlashCommentsP: Boolean = false
    private var slashStarCommentsP: Boolean = false
    private val ctype = ByteArray(256)

    actual var ttype: Int = TT_NOTHING
    actual var sval: String? = null
    actual var nval: Double = 0.0

    private constructor() {
        wordChars('a'.code, 'z'.code)
        wordChars('A'.code, 'Z'.code)
        wordChars(128 + 32, 255)
        whitespaceChars(0, ' '.code)
        commentChar('/'.code)
        quoteChar('"'.code)
        quoteChar('\''.code)
        parseNumbers()
    }

    actual constructor(isStream: InputStream) : this() {
        this.inputStreamRef = isStream
    }

    actual constructor(r: Reader) : this() {
        this.readerRef = r
    }

    private var readerRef: Reader? = null
    private var inputStreamRef: InputStream? = null

    actual open fun resetSyntax() {
        for (i in ctype.indices) {
            ctype[i] = 0
        }
    }

    actual open fun wordChars(low: Int, hi: Int) {
        var l = if (low < 0) 0 else low
        val h = if (hi >= ctype.size) ctype.size - 1 else hi
        while (l <= h) {
            ctype[l] = (ctype[l].toInt() or CT_ALPHA.toInt()).toByte()
            l++
        }
    }

    actual open fun whitespaceChars(low: Int, hi: Int) {
        var l = if (low < 0) 0 else low
        val h = if (hi >= ctype.size) ctype.size - 1 else hi
        while (l <= h) {
            ctype[l] = CT_WHITESPACE
            l++
        }
    }

    actual open fun ordinaryChars(low: Int, hi: Int) {
        var l = if (low < 0) 0 else low
        val h = if (hi >= ctype.size) ctype.size - 1 else hi
        while (l <= h) {
            ctype[l] = 0
            l++
        }
    }

    actual open fun ordinaryChar(ch: Int) {
        if (ch in 0 until ctype.size) {
            ctype[ch] = 0
        }
    }

    actual open fun commentChar(ch: Int) {
        if (ch in 0 until ctype.size) {
            ctype[ch] = CT_COMMENT
        }
    }

    actual open fun quoteChar(ch: Int) {
        if (ch in 0 until ctype.size) {
            ctype[ch] = CT_QUOTE
        }
    }

    actual open fun parseNumbers() {
        for (i in '0'.code..'9'.code) {
            ctype[i] = (ctype[i].toInt() or CT_DIGIT.toInt()).toByte()
        }
        ctype['.'.code] = (ctype['.'.code].toInt() or CT_DIGIT.toInt()).toByte()
        ctype['-'.code] = (ctype['-'.code].toInt() or CT_DIGIT.toInt()).toByte()
    }

    actual open fun eolIsSignificant(flag: Boolean) {
        eolIsSignificantP = flag
    }

    actual open fun slashStarComments(flag: Boolean) {
        slashStarCommentsP = flag
    }

    actual open fun slashSlashComments(flag: Boolean) {
        slashSlashCommentsP = flag
    }

    actual open fun lowerCaseMode(fl: Boolean) {
        forceLower = fl
    }

    private fun read(): Int {
        val r = readerRef
        if (r != null) return r.read()
        val s = inputStreamRef
        if (s != null) return s.read()
        throw IllegalStateException()
    }

    actual open fun nextToken(): Int {
        if (pushedBack) {
            pushedBack = false
            return ttype
        }
        val ct = ctype
        sval = null

        var c = peekc
        if (c < 0) c = NEED_CHAR
        if (c == SKIP_LF) {
            c = read()
            if (c < 0) {
                ttype = TT_EOF
                return ttype
            }
            if (c == '\n'.code) c = NEED_CHAR
        }
        if (c == NEED_CHAR) {
            c = read()
            if (c < 0) {
                ttype = TT_EOF
                return ttype
            }
        }
        ttype = c
        peekc = NEED_CHAR

        var ctypeVal = if (c < 256) ct[c].toInt() else CT_ALPHA.toInt()
        while ((ctypeVal and CT_WHITESPACE.toInt()) != 0) {
            if (c == '\r'.code) {
                lineNumber++
                if (eolIsSignificantP) {
                    peekc = SKIP_LF
                    ttype = TT_EOL
                    return ttype
                }
                c = read()
                if (c == '\n'.code) c = read()
            } else {
                if (c == '\n'.code) {
                    lineNumber++
                    if (eolIsSignificantP) {
                        ttype = TT_EOL
                        return ttype
                    }
                }
                c = read()
            }
            if (c < 0) {
                ttype = TT_EOF
                return ttype
            }
            ctypeVal = if (c < 256) ct[c].toInt() else CT_ALPHA.toInt()
        }

        if ((ctypeVal and CT_DIGIT.toInt()) != 0) {
            var neg = false
            if (c == '-'.code) {
                c = read()
                if (c != '.'.code && (c < '0'.code || c > '9'.code)) {
                    peekc = c
                    ttype = '-'.code
                    return ttype
                }
                neg = true
            }
            var v = 0.0
            var decexp = 0
            var seendot = 0
            while (true) {
                if (c == '.'.code && seendot == 0) {
                    seendot = 1
                } else if (c in '0'.code..'9'.code) {
                    v = v * 10 + (c - '0'.code)
                    decexp += seendot
                } else {
                    break
                }
                c = read()
            }
            peekc = c
            if (decexp != 0) {
                var div = 10.0
                decexp--
                while (decexp > 0) {
                    div *= 10.0
                    decexp--
                }
                v /= div
            }
            nval = if (neg) -v else v
            ttype = TT_NUMBER
            return ttype
        }

        if ((ctypeVal and CT_ALPHA.toInt()) != 0) {
            var i = 0
            do {
                if (i >= buf.size) {
                    buf = buf.copyOf(buf.size * 2)
                }
                buf[i++] = c.toChar()
                c = read()
                ctypeVal = if (c < 0) CT_WHITESPACE.toInt() else if (c < 256) ct[c].toInt() else CT_ALPHA.toInt()
            } while ((ctypeVal and (CT_ALPHA.toInt() or CT_DIGIT.toInt())) != 0)
            peekc = c
            val word = buf.concatToString(0, i)
            sval = if (forceLower) word.lowercase() else word
            ttype = TT_WORD
            return ttype
        }

        if ((ctypeVal and CT_QUOTE.toInt()) != 0) {
            ttype = c
            var i = 0
            var d = read()
            while (d >= 0 && d != ttype && d != '\n'.code && d != '\r'.code) {
                if (d == '\\'.code) {
                    c = read()
                    val first = c
                    if (c in '0'.code..'7'.code) {
                        c -= '0'.code
                        val c2 = read()
                        if (c2 in '0'.code..'7'.code) {
                            c = (c shl 3) + (c2 - '0'.code)
                            val c3 = read()
                            if (c3 in '0'.code..'7'.code && first <= '3'.code) {
                                c = (c shl 3) + (c3 - '0'.code)
                                d = read()
                            } else {
                                d = c3
                            }
                        } else {
                            d = c2
                        }
                    } else {
                        c = when (c) {
                            'a'.code -> 0x7
                            'b'.code -> '\b'.code
                            'f'.code -> 0xC
                            'n'.code -> '\n'.code
                            'r'.code -> '\r'.code
                            't'.code -> '\t'.code
                            'v'.code -> 0xB
                            else -> c
                        }
                        d = read()
                    }
                } else {
                    c = d
                    d = read()
                }
                if (i >= buf.size) {
                    buf = buf.copyOf(buf.size * 2)
                }
                buf[i++] = c.toChar()
            }
            peekc = if (d == ttype) NEED_CHAR else d
            sval = buf.concatToString(0, i)
            return ttype
        }

        if (c == '/'.code && (slashSlashCommentsP || slashStarCommentsP)) {
            c = read()
            if (c == '*'.code && slashStarCommentsP) {
                var prevc = 0
                while (true) {
                    c = read()
                    if (c == '/'.code && prevc == '*'.code) {
                        return nextToken()
                    }
                    if (c == '\r'.code) {
                        lineNumber++
                        c = read()
                        if (c == '\n'.code) {
                            c = read()
                        }
                    } else if (c == '\n'.code) {
                        lineNumber++
                        c = read()
                    }
                    if (c < 0) {
                        ttype = TT_EOF
                        return ttype
                    }
                    prevc = c
                }
            } else if (c == '/'.code && slashSlashCommentsP) {
                while (true) {
                    c = read()
                    if (c == '\n'.code || c == '\r'.code || c < 0) break
                }
                peekc = c
                return nextToken()
            } else {
                if ((ct['/'.code].toInt() and CT_COMMENT.toInt()) != 0) {
                    while (c != '\n'.code && c != '\r'.code && c >= 0) {
                        c = read()
                    }
                    peekc = c
                    return nextToken()
                } else {
                    peekc = c
                    ttype = '/'.code
                    return ttype
                }
            }
        }

        if ((ctypeVal and CT_COMMENT.toInt()) != 0) {
            while (true) {
                c = read()
                if (c == '\n'.code || c == '\r'.code || c < 0) break
            }
            peekc = c
            return nextToken()
        }

        ttype = c
        return ttype
    }

    actual open fun pushBack() {
        if (ttype != TT_NOTHING) {
            pushedBack = true
        }
    }

    actual open fun lineno(): Int = lineNumber

    actual override fun toString(): String {
        val ret = when (ttype) {
            TT_EOF -> "EOF"
            TT_EOL -> "EOL"
            TT_WORD -> sval
            TT_NUMBER -> "n=$nval"
            TT_NOTHING -> "NOTHING"
            else -> {
                if (ttype < 256 && (ctype[ttype].toInt() and CT_QUOTE.toInt()) != 0) {
                    sval
                } else {
                    val s = CharArray(3)
                    s[0] = '\''
                    s[2] = '\''
                    s[1] = ttype.toChar()
                    s.concatToString()
                }
            }
        }
        return "Token[$ret], line $lineNumber"
    }

    actual companion object {
        actual val TT_EOF: Int = -1
        actual val TT_EOL: Int = '\n'.code
        actual val TT_NUMBER: Int = -2
        actual val TT_WORD: Int = -3
    }
}

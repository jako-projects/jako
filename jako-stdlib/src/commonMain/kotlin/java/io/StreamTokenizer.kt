@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class StreamTokenizer {
    var ttype: Int
    var sval: String?
    var nval: Double

    constructor(isStream: InputStream)
    constructor(r: Reader)

    open fun resetSyntax()
    open fun wordChars(low: Int, hi: Int)
    open fun whitespaceChars(low: Int, hi: Int)
    open fun ordinaryChars(low: Int, hi: Int)
    open fun ordinaryChar(ch: Int)
    open fun commentChar(ch: Int)
    open fun quoteChar(ch: Int)
    open fun parseNumbers()
    open fun eolIsSignificant(flag: Boolean)
    open fun slashStarComments(flag: Boolean)
    open fun slashSlashComments(flag: Boolean)
    open fun lowerCaseMode(fl: Boolean)
    open fun nextToken(): Int
    open fun pushBack()
    open fun lineno(): Int
    override fun toString(): String

    companion object {
        val TT_EOF: Int
        val TT_EOL: Int
        val TT_NUMBER: Int
        val TT_WORD: Int
    }
}

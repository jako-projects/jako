package java.nio.charset

actual class CodingErrorAction {
    private val nameStr: String

    constructor() : this("REPORT")

    private constructor(name: String) {
        this.nameStr = name
    }

    actual override fun toString(): String = nameStr

    actual companion object {
        actual val IGNORE: CodingErrorAction = CodingErrorAction("IGNORE")
        actual val REPLACE: CodingErrorAction = CodingErrorAction("REPLACE")
        actual val REPORT: CodingErrorAction = CodingErrorAction("REPORT")
    }
}

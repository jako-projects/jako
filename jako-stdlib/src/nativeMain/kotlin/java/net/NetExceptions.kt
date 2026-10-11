package java.net

import java.io.IOException

actual open class MalformedURLException : IOException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class URISyntaxException : Exception {
    private val _input: String
    private val _reason: String
    private val _index: Int

    actual open val input: String get() = _input
    actual open val reason: String get() = _reason
    actual open val index: Int get() = _index

    actual constructor(input: String, reason: String, index: Int) : super("$reason at index $index: $input") {
        this._input = input
        this._reason = reason
        this._index = index
    }

    actual constructor(input: String, reason: String) : this(input, reason, -1)

    actual open fun getInput(): String = input
    actual open fun getReason(): String = reason
    actual open fun getIndex(): Int = index
}


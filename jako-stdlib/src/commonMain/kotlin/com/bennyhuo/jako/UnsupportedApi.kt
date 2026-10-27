package com.bennyhuo.jako

/**
 * Throws a standardized [UnsupportedOperationException] when a ported JDK or Android API
 * cannot be supported on a specific target runtime due to fundamental platform constraints
 * (e.g., preemptive shared-memory `Thread.start()` on single-threaded JS/Wasm runtimes).
 *
 * Message format:
 * `[jako] API '<api>' is not supported on target '<target>': <reason>. [Recommended alternative: <alternative>.]`
 */
fun unsupportedOnTarget(
    api: String,
    target: String,
    reason: String,
    alternative: String? = null
): Nothing {
    val suffix = if (!alternative.isNullOrBlank()) {
        " Recommended alternative: $alternative."
    } else {
        ""
    }
    throw UnsupportedOperationException(
        "[jako] API '$api' is not supported on target '$target': $reason.$suffix"
    )
}

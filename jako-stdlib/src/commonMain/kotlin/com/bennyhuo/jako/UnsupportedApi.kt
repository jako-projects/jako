package com.bennyhuo.jako

import com.bennyhuo.jako.annotations.WebRuntime

internal var simulatedWebRuntime: WebRuntime? = null

/**
 * Temporarily overrides the perceived [WebRuntime] on Web/Wasm targets for testing runtime capability boundaries
 * (e.g., simulating [WebRuntime.BROWSER] or [WebRuntime.WASI] under Node.js test runners).
 */
fun <T> withSimulatedWebRuntime(runtime: WebRuntime?, block: () -> T): T {
    val prev = simulatedWebRuntime
    simulatedWebRuntime = runtime
    try {
        return block()
    } finally {
        simulatedWebRuntime = prev
    }
}

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

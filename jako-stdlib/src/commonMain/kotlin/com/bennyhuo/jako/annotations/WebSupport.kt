package com.bennyhuo.jako.annotations

/**
 * Web 与 WebAssembly 平台下的细分宿主运行时环境。
 */
enum class WebRuntime {
    /** Node.js 运行时（含 jsNode 与 wasmJsNode） */
    NODE_JS,
    /** WebAssembly System Interface Preview 1 运行时（wasmWasi） */
    WASI,
    /** 浏览器沙箱运行时（含 jsBrowser 与 wasmJsBrowser） */
    BROWSER
}

/**
 * 声明该 API 在 Web/Wasm 平台下仅在指定的 [runtimes] 运行时中受支持。
 *
 * 规则说明：
 * 1. 在 JVM、Android、Native 平台上，所有公开 API 均始终受支持。
 * 2. 未标注此注解的公开 API 默认在全部 [WebRuntime] (`NODE_JS`, `WASI`, `BROWSER`) 下均受支持。
 * 3. 若 [runtimes] 为空（即 `@WebSupport()`），表示在所有 Web/Wasm 运行时下均不支持（仅 JVM/Android/Native 支持）。
 * 4. 标注在类/接口上时，默认作用于该类的所有构造函数、成员方法与属性（成员若有更严格的注解则以成员注解为准）。
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.BINARY)
annotation class WebSupport(
    vararg val runtimes: WebRuntime,
    val reason: String = "",
    val alternative: String = ""
)

@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
expect open class FileReader : InputStreamReader {
    constructor(file: File)
    constructor(fileName: String)
}

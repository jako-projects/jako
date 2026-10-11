package kotlin.text

import java.util.regex.Pattern

public fun Pattern.toRegex(): Regex = Regex(pattern())

public fun Regex.toPattern(): Pattern = Pattern.compile(pattern)

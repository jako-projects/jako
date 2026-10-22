@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect enum class LinkOption : OpenOption, CopyOption {
    NOFOLLOW_LINKS
}

@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes

expect interface FileVisitor<T> {
    // [Nullability Decision] Java T is a platform type; walkFileTree always supplies a non-null path.
    // [Nullability Decision] attrs is BasicFileAttributes! in Java; Files always passes a non-null instance.
    fun preVisitDirectory(dir: T, attrs: BasicFileAttributes): FileVisitResult
    fun visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult
    // [Nullability Decision] visitFileFailed exc is IOException!; Files never passes null.
    fun visitFileFailed(file: T, exc: IOException): FileVisitResult
    // [Nullability Decision] postVisitDirectory exc is IOException; null means the directory was visited without error.
    fun postVisitDirectory(dir: T, exc: IOException?): FileVisitResult
}

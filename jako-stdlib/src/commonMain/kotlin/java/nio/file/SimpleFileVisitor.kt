@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes

expect open class SimpleFileVisitor<T> : FileVisitor<T> {
    protected constructor()
    override fun preVisitDirectory(dir: T, attrs: BasicFileAttributes): FileVisitResult
    override fun visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult
    override fun visitFileFailed(file: T, exc: IOException): FileVisitResult
    override fun postVisitDirectory(dir: T, exc: IOException?): FileVisitResult
}

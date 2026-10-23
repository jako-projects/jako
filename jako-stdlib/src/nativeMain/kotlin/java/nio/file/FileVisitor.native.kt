package java.nio.file

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.nativeReadBasicAttributes

actual interface FileVisitor<T> {
    actual fun preVisitDirectory(dir: T, attrs: BasicFileAttributes): FileVisitResult
    actual fun visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult
    actual fun visitFileFailed(file: T, exc: IOException): FileVisitResult
    actual fun postVisitDirectory(dir: T, exc: IOException?): FileVisitResult
}

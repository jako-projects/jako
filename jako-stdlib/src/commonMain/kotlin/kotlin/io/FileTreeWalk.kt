package kotlin.io

import java.io.File
import java.io.IOException

public enum class FileWalkDirection {
    TOP_DOWN,
    BOTTOM_UP
}

public class FileTreeWalk private constructor(
    private val start: File,
    private val direction: FileWalkDirection = FileWalkDirection.TOP_DOWN,
    private val onEnter: ((File) -> Boolean)?,
    private val onLeave: ((File) -> Unit)?,
    private val onFail: ((File, IOException) -> Unit)?,
    private val maxDepth: Int = Int.MAX_VALUE
) : Sequence<File> {

    internal constructor(start: File, direction: FileWalkDirection = FileWalkDirection.TOP_DOWN) : this(
        start, direction, null, null, null
    )

    public override fun iterator(): Iterator<File> = FileTreeWalkIterator()

    public fun onEnter(function: (File) -> Boolean): FileTreeWalk =
        FileTreeWalk(start, direction, onEnter = function, onLeave = onLeave, onFail = onFail, maxDepth = maxDepth)

    public fun onLeave(function: (File) -> Unit): FileTreeWalk =
        FileTreeWalk(start, direction, onEnter = onEnter, onLeave = function, onFail = onFail, maxDepth = maxDepth)

    public fun onFail(function: (File, IOException) -> Unit): FileTreeWalk =
        FileTreeWalk(start, direction, onEnter = onEnter, onLeave = onLeave, onFail = function, maxDepth = maxDepth)

    public fun maxDepth(depth: Int): FileTreeWalk {
        if (depth <= 0) {
            throw IllegalArgumentException("depth must be positive, but was $depth.")
        }
        return FileTreeWalk(start, direction, onEnter, onLeave, onFail, depth)
    }

    private inner class FileTreeWalkIterator : Iterator<File> {
        private val results = ArrayList<File>()
        private var index = 0

        init {
            if (start.exists()) {
                walkRecursive(start, 1)
            }
        }

        private fun walkRecursive(current: File, depth: Int) {
            if (!current.isDirectory()) {
                results.add(current)
                return
            }
            if (onEnter != null && !onEnter.invoke(current)) {
                return
            }
            if (direction == FileWalkDirection.TOP_DOWN) {
                results.add(current)
            }
            val children = current.listFiles()
            if (children == null) {
                onFail?.invoke(current, IOException("Cannot list files in a directory"))
            } else {
                for (child in children) {
                    if (!child.isDirectory() || depth >= maxDepth) {
                        results.add(child)
                    } else {
                        walkRecursive(child, depth + 1)
                    }
                }
            }
            if (direction == FileWalkDirection.BOTTOM_UP) {
                results.add(current)
            }
            onLeave?.invoke(current)
        }

        override fun hasNext(): Boolean = index < results.size

        override fun next(): File {
            if (!hasNext()) throw NoSuchElementException()
            return results[index++]
        }
    }
}

public fun File.walk(direction: FileWalkDirection = FileWalkDirection.TOP_DOWN): FileTreeWalk =
    FileTreeWalk(this, direction)

public fun File.walkTopDown(): FileTreeWalk = walk(FileWalkDirection.TOP_DOWN)

public fun File.walkBottomUp(): FileTreeWalk = walk(FileWalkDirection.BOTTOM_UP)

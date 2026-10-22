@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect abstract class ObjectStreamException : IOException {
    protected constructor(classname: String?)
    protected constructor()
}

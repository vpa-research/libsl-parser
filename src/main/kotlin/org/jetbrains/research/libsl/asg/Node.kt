package org.jetbrains.research.libsl.asg

sealed class Node : IPrinter {
    override fun toString(): String = dumpToString()
}


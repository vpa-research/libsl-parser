package org.jetbrains.research.libsl.nodes

sealed class Node : IPrinter {
    override fun toString(): String = dumpToString()
}


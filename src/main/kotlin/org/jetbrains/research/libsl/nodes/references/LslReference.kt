package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase

interface LslReference<T, R> {
    val context: LslContextBase

    fun resolve(): T?

    fun isReferenceMatchWithNode(node: T): Boolean

    fun isSameReference(other: R): Boolean

    fun resolveOrError(): T&Any {
        return resolve()
            ?: error("Unresolved reference $this")
    }
}

package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.type.Type

object TypeDumper {

    fun dumpResolvedType(type: Type): String {
        return buildString {
            append(if (type.isPointer) "*" else "")
            append(type.name)
            if(type.generics.isNotEmpty()) {
                append("<")
                append(type.generics.joinToString(separator = ", ") {
                    it.dumpToString()
                })
                append(">")
            }
        }
    }
}

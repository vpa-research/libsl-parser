package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.helpers.TypeDumper.dumpResolvedType
import org.jetbrains.research.libsl.nodes.references.TypeReference

data class Generic(
    val genericModifier: String,
    val typeReference: TypeReference
): Node() {
    override fun dumpToString(): String = buildString {
        if(genericModifier != "") {
            append("$genericModifier ")
        }
        append(dumpResolvedType(typeReference.resolveOrError()))
    }

}

enum class GenericModifier(val string: String) {
    IN("in"), OUT("out");

    companion object {
        fun fromString(str: String) = GenericModifier.values().first { op -> op.string == str }
    }
}
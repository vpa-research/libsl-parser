package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class Annotation(
    val name: String,
    val values: MutableList<AnnotationParams>?
) : IPrinter {
    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("annotation ${BackticksPolitics.forPeriodSeparated(name)}")
        if(values?.isNotEmpty() == true) {
            appendLine("(")
            appendLine(withIndent(values.joinToString(separator = ",\n" ) { value -> value.dumpToString() }))
            append(")")
        }
        appendLine(";")
    }
}

data class AnnotationParams(
    val name: String,
    val typeReference: TypeReference,
    val initialValue: Expression?
) : IPrinter {
    override fun dumpToString(): String = buildString {
        append("${BackticksPolitics.forIdentifier(name)}: " +
                BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: Type.UNRESOLVED_TYPE_SYMBOL) +
                " = ${initialValue?.dumpToString()}")
    }
}

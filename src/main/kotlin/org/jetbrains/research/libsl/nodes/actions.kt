package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class ActionDecl(
    val name: String,
    val values: MutableList<DeclaredActionParameter> = mutableListOf(),
    val annotations: MutableList<AnnotationReference>? = mutableListOf(),
    val returnType: TypeReference?
) : IPrinter {
    override fun dumpToString(): String = buildString {
        annotations?.joinToString() { annotation ->
            append(annotation.resolveOrError().invocationDumpToString())
        }
        append("define action ${BackticksPolitics.forIdentifier(name)}")

        append(
            values.joinToString(separator = ", ", prefix = "(", postfix = ")") { value -> value.dumpToString()}
        )

        if (returnType != null) {
            append(": ")
            append(returnType.resolve()?.fullName ?: Type.UNRESOLVED_TYPE_SYMBOL)
            appendLine(";")
        } else {
            appendLine(";")
        }
    }
}

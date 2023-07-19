package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class ActionDecl(
    val name: String,
    val argumentDescriptors: MutableList<ActionArgumentDescriptor> = mutableListOf(),
    val annotations: MutableList<AnnotationUsage> = mutableListOf(),
    val returnType: TypeReference?
) : IPrinter {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotations))
        append("define action ${BackticksPolitics.forIdentifier(name)}")

        if (argumentDescriptors.isNotEmpty()) {
            appendLine("(")
            appendLine(
                withIndent(
                    argumentDescriptors.joinToString(
                        separator = ",\n",
                        transform = ActionArgumentDescriptor::dumpToString
                    )
                )
            )
            append(")")
        }

        if (returnType != null) {
            append(": ")
            append(returnType.resolve()?.fullName ?: Type.UNRESOLVED_TYPE_SYMBOL)
        }

        appendLine(";")
    }
}

data class ActionArgumentDescriptor(
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    val name: String,
    val typeReference: TypeReference
) : IPrinter {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        val type = BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: Type.UNRESOLVED_TYPE_SYMBOL)
        append("${BackticksPolitics.forIdentifier(name)}: $type")
    }
}

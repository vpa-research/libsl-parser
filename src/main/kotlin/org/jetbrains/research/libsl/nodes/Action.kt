package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition

data class Action(
    val name: String,
    val argumentDescriptors: MutableList<ActionArgumentDescriptor> = mutableListOf(),
    val annotations: MutableList<AnnotationUsage> = mutableListOf(),
    val returnType: TypeReference?,
    val entityPosition: EntityPosition
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
    val name: String,
    val typeReference: TypeReference,
    val entityPosition: EntityPosition
) : IPrinter {
    override fun dumpToString(): String = buildString {
        val type = BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: Type.UNRESOLVED_TYPE_SYMBOL)
        append("${BackticksPolitics.forIdentifier(name)}: $type")
    }
}

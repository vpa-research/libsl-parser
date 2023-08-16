package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition

data class Annotation(
    val name: String,
    val argumentDescriptors: MutableList<AnnotationArgumentDescriptor> = mutableListOf(),
    val entityPosition: EntityPosition
) : IPrinter {
    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("annotation ${BackticksPolitics.forPeriodSeparated(name)}")
        if (argumentDescriptors.isNotEmpty()) {
            appendLine("(")
            appendLine(
                withIndent(
                    argumentDescriptors.joinToString(
                        separator = ",\n",
                        transform = AnnotationArgumentDescriptor::dumpToString
                    )
                )
            )
            append(")")
        }
        appendLine(";")
    }
}

data class AnnotationArgumentDescriptor(
    val name: String,
    val typeReference: TypeReference,
    val initialValue: Expression?,
    val entityPosition: EntityPosition
) : IPrinter {
    override fun dumpToString(): String = buildString {
        val type = BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: Type.UNRESOLVED_TYPE_SYMBOL)
        append("${BackticksPolitics.forIdentifier(name)}: $type")
        if (initialValue != null) {
            append(" = ${initialValue.dumpToString()}")
        }
    }
}

data class AnnotationUsage(
    val annotationReference: AnnotationReference,
    val arguments: List<NamedArgumentWithValue>,
    val entityPosition: EntityPosition
) : IPrinter {
    override fun dumpToString() = buildString {
        append("@${BackticksPolitics.forIdentifier(annotationReference.resolveOrError().name)}(")
        if (arguments.isNotEmpty()) {
            append(
                arguments.joinToString(
                    separator = ", ",
                    transform = Expression::dumpToString
                )
            )
        }
        append(")")
    }
}

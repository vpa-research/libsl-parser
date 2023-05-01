package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class Annotation(
    val name: String,
    val argumentDescriptors: MutableList<AnnotationArgumentDescriptor>
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
    val initialValue: Expression?
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
    val arguments: List<Expression>
) : IPrinter {
    override fun dumpToString() = buildString {
        append("@${BackticksPolitics.forIdentifier(annotationReference.resolveOrError().name)}")
        if (arguments.isNotEmpty()) {
            append(
                arguments.joinToString(
                    prefix = "(",
                    separator = ", ",
                    postfix = ")",
                    transform = Expression::dumpToString
                )
            )
        }
    }
}

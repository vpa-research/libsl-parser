package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class Function(
    val name: String,
    val automatonReference: AutomatonReference,
    var args: MutableList<FunctionArgument> = mutableListOf(),
    val returnType: TypeReference?,
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    var localVariables: MutableList<Variable> = mutableListOf(),
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    var hasBody: Boolean = statements.isNotEmpty(),
    var targetAutomatonRef: AutomatonReference? = null,
    val context: FunctionContext
) : Node() {
    val fullName: String
        get() = "${automatonReference.name}.$name"

    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        append("fun ${BackticksPolitics.forIdentifier(name)}")
        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> arg.dumpToString() }
        )

        if (returnType != null) {
            append(": ")
            append(returnType.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)
        }

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        }
        if (contracts.isNotEmpty()) {
            appendLine(" {")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
            hasBody = true
        }

        if (hasBody) {
            if (contracts.isEmpty()) {
                appendLine(" {")
            }
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }
}

data class Constructor(
    var name: String?,
    var args: MutableList<FunctionArgument> = mutableListOf(),
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    var localVariables: MutableList<Variable> = mutableListOf(),
    val hasBody: Boolean = statements.isNotEmpty(),
    val context: FunctionContext
) : Node() {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))

        name = if (name.isNullOrEmpty()) {
            ""
        } else {
            " " + BackticksPolitics.forIdentifier(name!!)
        }

        append("constructor$name")

        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> arg.dumpToString() }
        )

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else if (hasBody) {
            appendLine(" {")
            if (contracts.isNotEmpty()) {
                append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
            }
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }

}

data class Destructor(
    var name: String?,
    var args: MutableList<FunctionArgument> = mutableListOf(),
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    var localVariables: MutableList<Variable> = mutableListOf(),
    val hasBody: Boolean = statements.isNotEmpty(),
    val context: FunctionContext
) : Node() {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))

        name = if (name.isNullOrEmpty()) {
            ""
        } else {
            " " + BackticksPolitics.forIdentifier(name!!)
        }

        append("destructor$name")

        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> arg.dumpToString() }
        )

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else if (hasBody) {
            appendLine(" {")
            if (contracts.isNotEmpty()) {
                append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
            }
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }
}

data class ProcDecl(
    val name: String,
    var args: MutableList<FunctionArgument> = mutableListOf(),
    val returnType: TypeReference?,
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    var localVariables: MutableList<Variable> = mutableListOf(),
    val hasBody: Boolean = statements.isNotEmpty(),
    val context: FunctionContext
) : Node() {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))

        append("proc ${BackticksPolitics.forIdentifier(name)}")
        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> arg.dumpToString() }
        )

        if (returnType != null) {
            append(": ")
            append(returnType.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)
        }

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else if (hasBody) {
            appendLine(" {")
            if (contracts.isNotEmpty()) {
                append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
            }
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }
}

data class ArgumentWithValue(
    val name: String,
    val value: Expression
) : IPrinter {
    override fun dumpToString(): String = "${BackticksPolitics.forIdentifier(name)} = ${value.dumpToString()}"
}

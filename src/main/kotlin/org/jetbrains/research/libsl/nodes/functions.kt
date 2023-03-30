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
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    val hasBody: Boolean = statements.isNotEmpty(),
    var targetAutomatonRef: AutomatonReference? = null,
    val context: FunctionContext
) : Node() {
    val fullName: String
        get() = "${automatonReference.name}.$name"
    var resultVariable: Variable? = null

    override fun dumpToString(): String = buildString {
        append("fun ${BackticksPolitics.forIdentifier(name)}")

        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> arg.dumpToString()}
        )

        if (returnType != null) {
            append(": ")
            append(returnType.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)
        }

        if (contracts.isNotEmpty()) {
            appendLine()
            append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
        }

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else if (hasBody) {
            if (contracts.isEmpty()) {
                append(IPrinter.SPACE)
            }
            appendLine("{")
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
package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.visitors.addBacktickIfNeeded

data class Function(
    val name: String,
    val automatonName: String,
    var args: MutableList<FunctionArgument> = mutableListOf(),
    val returnType: Type?,
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    val hasBody: Boolean = statements.isNotEmpty(),
    var target: Automaton? = null,
    val context: LslContext
) : Node() {
    val automaton: Automaton by lazy { context.resolveAutomaton(automatonName) ?: error("unresolved automaton") }
    val fullName: String
        get() = "${automaton.name}.$name"
    var resultVariable: Variable? = null

    override fun dumpToString(): String = buildString {
        append("fun ${addBacktickIfNeeded(name)}")

        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> buildString {
                if (arg.annotation != null) {
                    append("@")
                    append(addBacktickIfNeeded(arg.annotation!!.name))

                    if (arg.annotation!!.values.isNotEmpty()) {
                        append("(")
                        append(arg.annotation!!.values.joinToString(separator = ", ") { v ->
                            v.dumpToString()
                        })
                        append(")")
                    }

                    append(IPrinter.SPACE)
                }
                append(arg.name)
                append(": ")

                if (arg.annotation != null && arg.annotation is TargetAnnotation) {
                    append((arg.annotation as TargetAnnotation).targetAutomaton.name)
                } else {
                    append(arg.type.fullName)
                }
            } }
        )

        if (returnType != null) {
            append(": ")
            append(returnType.fullName)
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
    val variable: Variable,
    val init: Expression
) : IPrinter {
    override fun dumpToString(): String = "${variable.name} = ${init.dumpToString()}"
}
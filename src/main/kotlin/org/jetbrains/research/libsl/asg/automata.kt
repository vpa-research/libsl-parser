package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.utils.BackticksPolitics

data class Automaton(
    val name: String,
    val type: Type,
    val states: MutableList<State> = mutableListOf(),
    val shifts: MutableList<Shift> = mutableListOf(),
    val internalVariableDeclarations: MutableList<AutomatonVariableDeclaration> = mutableListOf(),
    val constructorVariables: MutableList<ConstructorArgument> = mutableListOf(),
    val localFunctions: MutableList<Function> = mutableListOf(),
    val extensionFunctions: MutableList<Function> = mutableListOf()
) : Node() {
    val functions: List<Function>
        get() = localFunctions + extensionFunctions
    val variables: List<Variable>
        get() = internalVariableDeclarations.map { decl -> decl.variable } + constructorVariables

    override fun dumpToString(): String = buildString {
        append("automaton ${BackticksPolitics.forPeriodSeparated(name)}")
        if (constructorVariables.isNotEmpty()) {
            append(" (${constructorVariables.joinToString(", ") { v -> v.dumpToString() } })")
        }
        appendLine(" : ${BackticksPolitics.forPeriodSeparated(type.fullName)} {")

        append(withIndent(formatBody()))
        append("}")
    }

    private fun formatBody(): String = buildString {
        append(formatStates())
        append(formatShifts())
        append(formatInternalVariables())
        append(formatFunctions())
    }

    private fun formatInternalVariables(): String = formatListEmptyLineAtEndIfNeeded(internalVariableDeclarations)

    private fun formatStates(): String = formatListEmptyLineAtEndIfNeeded(states)

    private fun formatShifts(): String = formatListEmptyLineAtEndIfNeeded(shifts)

    private fun formatFunctions(): String = formatListEmptyLineAtEndIfNeeded(functions, appendEndLineAtTheEnd = false)

    override fun toString(): String = dumpToString()
}

data class State(
    val name: String,
    val kind: StateKind,
    val isSelf: Boolean = false,
    val isAny: Boolean = false,
) : Node() {
    lateinit var automaton: Automaton

    override fun dumpToString(): String = "${kind.keyword} $name;"
}

data class Shift(
    val from: State,
    val to: State,
    val functions: MutableList<Function> = mutableListOf()
) : Node() {
    override fun dumpToString(): String = buildString {
        append("shift ")
        append(BackticksPolitics.forIdentifier(from.name))
        append(" -> ")
        append(BackticksPolitics.forTypeIdentifier(to.name))

        if (functions.isNotEmpty()) {
            append(
                functions.joinToString(separator = ", ", prefix = "(", postfix = ")") { function -> function.name }
            )
        }

        appendLine(";")
    }
}

enum class StateKind(val keyword: String) {
    INIT("initstate"), SIMPLE("state"), FINISH("finishstate");

    internal companion object {
        fun fromString(str: String): StateKind {
            return StateKind.values().firstOrNull { state -> state.keyword == str } ?: error("unknown state kind: $str")
        }
    }
}
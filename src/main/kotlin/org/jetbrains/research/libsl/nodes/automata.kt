package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class Automaton(
    val name: String,
    val typeReference: TypeReference,
    val annotations: MutableList<AnnotationReference>? = mutableListOf(),
    val states: MutableList<State> = mutableListOf(),
    val shifts: MutableList<Shift> = mutableListOf(),
    val internalVariables: MutableList<VariableWithInitialValue> = mutableListOf(),
    val constructorVariables: MutableList<ConstructorArgument> = mutableListOf(),
    val localFunctions: MutableList<Function> = mutableListOf(),
    val extensionFunctions: MutableList<Function> = mutableListOf(),
    val context: AutomatonContext
) : Node() {
    val functions: List<Function>
        get() = localFunctions + extensionFunctions

    override fun dumpToString(): String = buildString {
        annotations?.joinToString() { annotation ->
            append(annotation.resolveOrError().invocationDumpToString())
        }

        append("automaton ${BackticksPolitics.forPeriodSeparated(name)}")
        if (constructorVariables.isNotEmpty()) {
            append(" (${constructorVariables.joinToString(", ") { v -> v.dumpToString() } })")
        }
        appendLine(" : ${BackticksPolitics.forPeriodSeparated(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)} {")

        append(withIndent(formatBody()))
        append("}")
    }

    private fun formatBody(): String = buildString {
        append(formatStates())
        append(formatShifts())
        append(formatInternalVariables())
        append(formatFunctions())
    }

    private fun formatInternalVariables(): String = formatListEmptyLineAtEndIfNeeded(internalVariables)

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
    val functions: MutableList<FunctionReference> = mutableListOf()
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

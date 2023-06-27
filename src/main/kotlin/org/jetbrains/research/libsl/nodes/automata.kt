package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.Position

open class Automaton(
    open val isConcept: Boolean,
    open val name: String,
    open val typeReference: TypeReference,
    open val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    open val implementedConcepts: MutableList<ImplementedConcept> = mutableListOf(),
    open val states: MutableList<State> = mutableListOf(),
    open val shifts: MutableList<Shift> = mutableListOf(),
    open val internalVariables: MutableList<VariableWithInitialValue> = mutableListOf(),
    open val constructorVariables: MutableList<ConstructorArgument> = mutableListOf(),
    open val constructors: MutableList<Function> = mutableListOf(),
    open val destructors: MutableList<Function> = mutableListOf(),
    open val procDeclarations: MutableList<Function> = mutableListOf(),
    open val localFunctions: MutableList<Function> = mutableListOf(),
    open val extensionFunctions: MutableList<Function> = mutableListOf(),
    open val context: AutomatonContext,
    open val position: Position
) : Node() {
    open val functions: List<Function>
        get() = localFunctions + extensionFunctions

    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        append("automaton ${BackticksPolitics.forPeriodSeparated(name)}")

        if (constructorVariables.isNotEmpty()) {
            append(" (${constructorVariables.joinToString(", ") { v -> v.dumpToString() } })")
        }
        append(" : ${BackticksPolitics.forPeriodSeparated(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)}")

        if(implementedConcepts.isNotEmpty()) {
            append(" implements ")
            append(implementedConcepts.joinToString(separator = ", ") {
                it.name
            })
        }

        appendLine(" {")

        append(withIndent(formatBody()))
        append("}")
    }

    private fun formatBody(): String = buildString {
        append(formatStates())
        append(formatShifts())
        append(formatInternalVariables())
        append(formatConstructors())
        append(formatDestructors())
        append(formatProcDeclarations())
        append(formatFunctions())
    }

    private fun formatInternalVariables(): String = formatListEmptyLineAtEndIfNeeded(internalVariables)

    private fun formatStates(): String = formatListEmptyLineAtEndIfNeeded(states)

    private fun formatShifts(): String = formatListEmptyLineAtEndIfNeeded(shifts)

    private fun formatConstructors(): String = formatListEmptyLineAtEndIfNeeded(constructors)

    private fun formatDestructors(): String = formatListEmptyLineAtEndIfNeeded(destructors)

    private fun formatProcDeclarations(): String = formatListEmptyLineAtEndIfNeeded(procDeclarations)

    private fun formatFunctions(): String = formatListEmptyLineAtEndIfNeeded(functions, appendEndLineAtTheEnd = false)

    override fun toString(): String = dumpToString()
}

data class AutomatonConcept(
    override val isConcept: Boolean,
    override val name: String,
    override val typeReference: TypeReference,
    override val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override val implementedConcepts: MutableList<ImplementedConcept> = mutableListOf(),
    override val states: MutableList<State> = mutableListOf(),
    override val shifts: MutableList<Shift> = mutableListOf(),
    override val internalVariables: MutableList<VariableWithInitialValue> = mutableListOf(),
    override val constructorVariables: MutableList<ConstructorArgument> = mutableListOf(),
    override val constructors: MutableList<Function> = mutableListOf(),
    override val destructors: MutableList<Function> = mutableListOf(),
    override val procDeclarations: MutableList<Function> = mutableListOf(),
    override val localFunctions: MutableList<Function> = mutableListOf(),
    override val extensionFunctions: MutableList<Function> = mutableListOf(),
    override val context: AutomatonContext,
    override val position: Position
) : Automaton(
    isConcept, name, typeReference, annotationUsages,
    implementedConcepts, states, shifts, internalVariables,
    constructorVariables, constructors, destructors, procDeclarations,
    localFunctions, extensionFunctions, context, position
) {
    override val functions: List<Function>
        get() = localFunctions + extensionFunctions

    override fun dumpToString(): String = buildString {

        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        append("automaton concept ${BackticksPolitics.forPeriodSeparated(name)}")
        if (constructorVariables.isNotEmpty()) {
            append(" (${constructorVariables.joinToString(", ") { v -> v.dumpToString() } })")
        }
        append(" : ${BackticksPolitics.forPeriodSeparated(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)}")

        if(implementedConcepts.isNotEmpty()) {
            append(" implements ")
            append(implementedConcepts.joinToString(separator = ", ") {
                it.name
            })
        }

        appendLine(" {")

        append(withIndent(formatBody()))
        append("}")
    }

    private fun formatBody(): String = buildString {
        append(formatStates())
        append(formatShifts())
        append(formatInternalVariables())
        append(formatConstructors())
        append(formatDestructors())
        append(formatProcDeclarations())
        append(formatFunctions())
    }

    private fun formatInternalVariables(): String = formatListEmptyLineAtEndIfNeeded(internalVariables)

    private fun formatStates(): String = formatListEmptyLineAtEndIfNeeded(states)

    private fun formatShifts(): String = formatListEmptyLineAtEndIfNeeded(shifts)

    private fun formatConstructors(): String = formatListEmptyLineAtEndIfNeeded(constructors)

    private fun formatDestructors(): String = formatListEmptyLineAtEndIfNeeded(destructors)

    private fun formatProcDeclarations(): String = formatListEmptyLineAtEndIfNeeded(procDeclarations)

    private fun formatFunctions(): String = formatListEmptyLineAtEndIfNeeded(functions, appendEndLineAtTheEnd = false)

    override fun toString(): String = dumpToString()
}

data class ImplementedConcept(
    val name: String
)

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
        append(" by ")

        if (functions.isNotEmpty()) {
            append(
                functions.joinToString(separator = ", ", prefix = "[", postfix = "]") { function ->
                    val functionName = function.name
                    if (function.argTypes.isNotEmpty()) {
                        val argTypeNames =
                            function.argTypes.joinToString(separator = ", ", prefix = "(", postfix = ")") { it.name }
                        "$functionName$argTypeNames"
                    } else {
                        functionName
                    }
                }
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

package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics

enum class ArithmeticUnaryOp(val string: String) {
    PLUS("+"), MINUS("-"), INVERSION("!"), TILDE("~");

    companion object {
        fun fromString(str: String) = ArithmeticUnaryOp.values().firstOrNull { op ->
            op.string == str }
                ?: throw NoSuchElementException("Unknown operator: $str")

    }
}

enum class VariableKind(val string: String) {
    VAR("var"), VAL("val");

    companion object {
        fun fromString(str: String) = VariableKind.values().first { op -> op.string == str }
    }
}

open class Variable(
    open var name: String,
    open var typeReference: TypeReference
) : Expression() {
    open val fullName: String
        get() = name

    override fun dumpToString(): String = name
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Variable) return false
        if (name != other.name) return false
        if (typeReference != other.typeReference) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + typeReference.hashCode()
        return result
    }
}

data class ResultVariable(
    override var typeReference: TypeReference
) : Variable(name = "result", typeReference)

@Suppress("unused")
class FunctionArgument(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    var targetAutomaton: AutomatonReference? = null
) : Variable(name, typeReference) {
    lateinit var function: Function

    override val fullName: String
        get() = "${function.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotationUsages.isNotEmpty()) {
            append(
                formatListEmptyLineAtEndIfNeeded(
                    annotationUsages,
                    appendEndLineAtTheEnd = false,
                    onSeparatedLines = false
                )
            )
            append(IPrinter.SPACE)
        }

        append(BackticksPolitics.forIdentifier(name))
        append(": ")
        if (targetAutomaton != null) {
            append(targetAutomaton!!.name)
        } else {
            append(typeReference.name)
        }
    }
}

@Suppress("unused")
class ActionParameter(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotation: AnnotationReference? = null
) : Variable(name, typeReference)

class ConstructorArgument(
    val keyword: VariableKind,
    name: String,
    typeReference: TypeReference,
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf()
) : Variable(name, typeReference) {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotationUsages.isNotEmpty()) {
            append(formatListEmptyLineAtEndIfNeeded(annotationUsages, onSeparatedLines = false))
            append(IPrinter.SPACE)
        }
        append("${keyword.string} ${BackticksPolitics.forIdentifier(name)}: ")
        append(BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL))
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class VariableWithInitialValue(
    val keyword: VariableKind,
    name: String,
    typeReference: TypeReference,
    val annotationUsage: MutableList<AnnotationUsage> = mutableListOf(),
    val initialValue: Expression?,
) : Variable(name, typeReference) {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsage))
        append("${keyword.string} ${BackticksPolitics.forIdentifier(name)}: ")
        append(BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL))
        if (initialValue != null) {
            append(" = ${initialValue.dumpToString()};")
        } else {
            append(";")
        }
    }
}

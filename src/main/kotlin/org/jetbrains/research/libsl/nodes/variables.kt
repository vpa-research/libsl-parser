package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics

enum class ArithmeticUnaryOp(val string: String) {
    MINUS("-"), INVERSION("!");

    companion object {
        fun fromString(str: String) = ArithmeticUnaryOp.values().first { op -> op.string == str }
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
    var annotationsReferences: MutableList<AnnotationReference>? = mutableListOf(),
    var targetAutomaton: AutomatonReference? = null
) : Variable(name, typeReference) {
    lateinit var function: Function

    override val fullName: String
        get() = "${function.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotationsReferences != null) {

            annotationsReferences?.joinToString() { annotation ->
                val resolvedAnnotation = annotation.resolveOrError()
                append("@")
                append(BackticksPolitics.forIdentifier(resolvedAnnotation.name))

                if (resolvedAnnotation.values.isNotEmpty()) {
                    append("(")
                    append(resolvedAnnotation.values.joinToString(separator = ", ") { v ->
                        v.dumpToString()
                    })
                    append(")")
                }

                append(IPrinter.SPACE)
            }
        }
        append(BackticksPolitics.forIdentifier(name))
        append(": ")

        val typeName = if (targetAutomaton != null) {
            targetAutomaton!!.name
        } else {
            typeReference.name
        }

        append(typeName)
    }
}

@Suppress("unused")
class ActionParameter(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotation: Annotation? = null,
) : Variable(name, typeReference) {

    override fun dumpToString(): String = buildString {
        if (annotation != null) {
            append("@")
            append(BackticksPolitics.forIdentifier(annotation!!.name))

            if (annotation!!.values.isNotEmpty()) {
                append("(")
                append(annotation!!.values.joinToString(separator = ", ") { v ->
                    v.dumpToString()
                })
                append(")")
            }

            append(IPrinter.SPACE)
        }
        append(BackticksPolitics.forIdentifier(name))
        append(": ")

        append(typeReference.name)
    }
}

class DeclaredActionParameter(
    name: String,
    typeReference: TypeReference,
    var annotation: Annotation? = null,
) : Variable(name, typeReference) {

    override fun dumpToString(): String = buildString {
        if (annotation != null) {
            append("@")
            append(BackticksPolitics.forIdentifier(annotation!!.name))

            if (annotation!!.values.isNotEmpty()) {
                append("(")
                append(annotation!!.values.joinToString(separator = ", ") { v ->
                    v.dumpToString()
                })
                append(")")
            }

            append(IPrinter.SPACE)
        }
        append(BackticksPolitics.forIdentifier(name))
        append(": ")

        append(typeReference.name)
    }
}

class ConstructorArgument(
    name: String,
    typeReference: TypeReference,
) : Variable(name, typeReference) {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.$name"

    override fun dumpToString(): String = "var ${BackticksPolitics.forIdentifier(name)}: " +
            BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)
}

class VariableWithInitialValue(
    name: String,
    typeReference: TypeReference,
    val initialValue: Expression?
) : Variable(name, typeReference) {
    override fun dumpToString() = "var ${BackticksPolitics.forIdentifier(name)}: " +
            BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL) +
            if (initialValue != null) " = ${initialValue.dumpToString()};" else ";"
}

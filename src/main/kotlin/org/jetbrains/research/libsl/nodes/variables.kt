package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.utils.BackticksPolitics

enum class ArithmeticUnaryOp(val string: String) {
    MINUS("-"), INVERSION("!");

    companion object {
        fun fromString(str: String) = ArithmeticUnaryOp.values().first { op -> op.string == str }
    }
}

open class Variable(
    open val name: String,
    open val typeReference: TypeReference
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
    override val typeReference: TypeReference
) : Variable(name = "result", typeReference)

sealed class VariableDeclaration : Node() {
    abstract val name: String
    abstract val typeReference: TypeReference
    abstract val initValue: Expression?

    override fun dumpToString(): String = buildString {
        append("var ${BackticksPolitics.forIdentifier(name)}: " +
                BackticksPolitics.forTypeIdentifier(typeReference.resolveOrError().fullName)
        )
        if (initValue != null) {
            append(" = ${initValue!!.dumpToString()}")
        }

        appendLine(";")
    }
}

data class VariableDeclarationImpl(
    override val name: String,
    override val typeReference: TypeReference,
    override val initValue: Expression?
) : VariableDeclaration()

@Suppress("unused")
class FunctionArgument(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotation: Annotation? = null
) : Variable(name, typeReference) {
    lateinit var function: Function

    override val fullName: String
        get() = "${function.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotation != null) {
            append("@${annotation!!.dumpToString()} ")
        }
        append(name)
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
            BackticksPolitics.forTypeIdentifier(typeReference.resolveOrError().fullName)
}

class VariableWithInitialValue(
    name: String,
    typeReference: TypeReference,
    val initialValue: Expression?
) : Variable(name, typeReference) {
    override fun dumpToString() = "var ${BackticksPolitics.forIdentifier(name)}: " +
            BackticksPolitics.forTypeIdentifier(typeReference.resolveOrError().fullName) +
            if (initialValue != null) " = ${initialValue.dumpToString()};" else ";"
}

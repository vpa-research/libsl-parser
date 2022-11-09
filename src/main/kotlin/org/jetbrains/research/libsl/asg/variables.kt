package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.visitors.addBacktickIfNeeded

enum class ArithmeticUnaryOp(val string: String) {
    MINUS("-"), INVERSION("!")
}

open class Variable(
    open val name: String,
    open val type: Type
) : Expression() {
    open val fullName: String
        get() = name

    override fun dumpToString(): String = name
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Variable) return false

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

data class ResultVariable(
    override val type: Type
) : Variable(name = "result", type)

sealed class VariableDeclaration : Node() {
    abstract val variable: Variable
    abstract val initValue: Expression?

    override fun dumpToString(): String = buildString {
        append("var ${addBacktickIfNeeded(variable.name)}: ${variable.type.fullName}")
        if (initValue != null) {
            append(" = ${initValue!!.dumpToString()}")
        }

        appendLine(";")
    }
}

data class GlobalVariableDeclaration(
    override val variable: Variable,
    override val initValue: Expression?
) : VariableDeclaration()

data class AutomatonVariableDeclaration(
    override val variable: Variable,
    override var initValue: Expression?
) : VariableDeclaration() {
    lateinit var automaton: Automaton
}

class FunctionArgument(
    name: String,
    type: Type,
    val index: Int,
    var annotation: Annotation? = null
) : Variable(name, type) {
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
    type: Type,
) : Variable(name, type) {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.$name"

    override fun dumpToString(): String = "var ${addBacktickIfNeeded(name)}: ${type.fullName}"
}
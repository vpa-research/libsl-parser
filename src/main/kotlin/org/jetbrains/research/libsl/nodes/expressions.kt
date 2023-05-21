package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.helpers.ExpressionDumper
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.AutomatonStateReference
import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class Expression : Node() {
    override fun dumpToString(): String = ExpressionDumper.dump(this)
}

data class BinaryOpExpression(
    val left: Expression,
    val right: Expression,
    val op: ArithmeticBinaryOps
) : Expression()

// priorities from https://www.l3harrisgeospatial.com/docs/Operator_Precedence.html
// additionally >>, <<, >>> (shifts) are 7 priority
enum class ArithmeticBinaryOps(val string: String, val priority: Int) {
    //Arithmetic
    ADD("+", 5), SUB("-", 5), MUL("*", 4), DIV("/", 4),
    MOD("%", 4),

    //Logic
    LOG_AND("&&", 8), LOG_OR("||", 8),

    //Bitwise
    XOR("^", 7), AND("&", 7), BIT_OR("|", 7),

    //Relational
    EQ("==", 6), NOT_EQ("!=", 6), LT_EQ("<=", 6), GT_EQ(">=", 6),
    GT(">", 6), LT("<", 6),

    //Shift
    R_SHIFT(">>", 7), UNSIGNED_R_SHIFT(">>>", 7), L_SHIFT("<<", 7);

    companion object {
        fun fromString(str: String) = ArithmeticBinaryOps.values().first { op -> op.string == str }
    }
}

enum class AssignOps(val string: String) {
    ASSIGN("="),

    //Arithmetic
    COMP_ADD("+="), COMP_SUB("-="), COMP_MUL("*="), COMP_DIV("/="), COMP_MOD("%="),

    //Bitwise
    COMP_AND("&="),  COMP_OR("|="), COMP_XOR("^="),

    //Shift
    COMP_R_SHIFT(">>="), COMP_UN_R_SHIFT(">>>="), COMP_L_SHIFT("<<=");

    companion object {
        fun fromString(str: String) = AssignOps.values().first { op -> op.string == str }
    }
}

data class UnaryOpExpression(
    val op: ArithmeticUnaryOp,
    val value: Expression
) : Expression()

data class OldValue(
    val value: QualifiedAccess
) : Expression()

data class CallAutomatonConstructor(
    val automatonRef: AutomatonReference,
    val args: List<NamedArgumentWithValue>,
    val stateRef: AutomatonStateReference
) : Atomic() {
    override val value: Any? = null

    override fun toString(): String = dumpToString()
}

data class ArrayLiteral(
    override val value: List<Expression>
) : Atomic()

sealed class Atomic : Expression() {
    abstract val value: Any?
}

data class ActionExpression(
    val action: Action
) : Expression()

data class ProcExpression(
    val procedureCall: ProcedureCall
) : Expression()

data class NamedArgumentWithValue(
    val name: String?,
    val value: Expression
) : Expression()

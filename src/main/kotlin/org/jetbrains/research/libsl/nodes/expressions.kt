package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.helpers.ExpressionDumper
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.AutomatonStateReference
import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class Expression: Node() {
    override fun dumpToString(): String = ExpressionDumper.dump(this)
}

data class ThisExpression(
    val thisKeywordUsed: Boolean,
    val parentKeywordUsed: Boolean
) : Expression()

data class BinaryOpExpression(
    val left: Expression,
    val right: Expression,
    val op: ArithmeticBinaryOps
) : Expression()

// priorities from https://www.l3harrisgeospatial.com/docs/Operator_Precedence.html
// additionally >>, <<, >>> (shifts) are 7 priority
enum class ArithmeticBinaryOps(val string: String, val priority: Int) {
    ADD("+", 5), SUB("-", 5), MUL("*", 4), DIV("/", 4),
    AND("&", 7), LOG_AND("&&", 8), BIT_OR("|", 7), LOG_OR("||", 8),
    XOR("^", 7), MOD("%", 4), EQ("==", 6), NOT_EQ("!=", 6),
    GT(">", 6), GT_EQ(">=", 6), LT("<", 6), LT_EQ("<=", 6),
    R_SHIFT(">>", 7), UNSIGNED_R_SHIFT(">>>", 7), L_SHIFT("<<", 7);
    companion object {
        fun fromString(str: String) = ArithmeticBinaryOps.values().first { op -> op.string == str }
    }
}

enum class CompoundOps(val string: String) {
    COMP_ADD("+="), COMP_SUB("-="), COMP_MUL("*="), COMP_DIV("/="), COMP_AND("&="),
    COMP_OR("|="), COMP_XOR("^="), COMP_MOD("%="), COMP_R_SHIFT(">>="), COMP_UN_R_SHIFT(">>>="),
    COMP_L_SHIFT("<<=");
    companion object {
        fun fromString(str: String) = CompoundOps.values().first { op -> op.string == str }
    }
}

data class UnaryOpExpression(
    val value: Expression,
    val op: ArithmeticUnaryOp
) : Expression()

data class OldValue(
    val value: QualifiedAccess
) : Expression()

data class CallAutomatonConstructor(
    val automatonRef: AutomatonReference,
    val args: List<ArgumentWithValue>,
    val stateRef: AutomatonStateReference,
    val parentRef: AutomatonReference?
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
    val proc: Proc
) : Expression()

data class LeftUnaryOpExpression(
    val op: ArithmeticUnaryOp,
    val value: Expression
) : Expression()

data class RightUnaryOpExpression(
    val op: ArithmeticUnaryOp,
    val value: Expression
) : Expression()

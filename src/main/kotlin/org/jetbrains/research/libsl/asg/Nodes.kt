package org.jetbrains.research.libsl.asg

sealed class Node {
    open val parent: NodeHolder = NodeHolder()
}

data class Library(
    val metadata: MetaNode,
    val imports: List<String>,
    val includes: List<String> ,
    val semanticTypes: List<Type>,
    val automata: List<Automaton>,
    val extensionFunctions: Map<String, List<Function>>
) : Node()

data class MetaNode(
    val name: String,
    val libraryVersion: String?,
    val language: String?,
    val url: String?,
    val lslVersion: Triple<UInt, UInt, UInt>?
) : Node() {
    val stringVersion: String?
        get() {
            if (lslVersion == null) return null
            return "${lslVersion.first}.${lslVersion.second}.${lslVersion.third}"
        }
}

sealed class Type: Node() {
    abstract val semanticType: String
    abstract val realType: RealType
    abstract val context: LslContext
}

data class SimpleType(
    override val semanticType: String,
    override val realType: RealType,
    override val context: LslContext
) : Type()

data class RealType(
    val name: List<String>,
    val generic: List<String>?
) : Node()

data class EnumLikeType(
    override val semanticType: String,
    override val realType: RealType,
    val entities: List<Pair<String, String>>,
    override val context: LslContext
) : Type()

data class Automaton(
    val name: String,
    val kind: AutomatonKind,
    var states: List<State>,
    var shifts: List<Shift>,
    var internalVariables: List<Variable>,
    var constructorVariables: List<Variable>,
    var localFunctions: List<Function>
) : Node() {
    val functions: List<Function>
        get() = localFunctions + (parent.node as Library).extensionFunctions[name].orEmpty()
}

data class VariablesBlock(
    val variables: List<Variable>
) : Node()

data class State(
    val name: String,
    val kind: StateKind,
    val isSelf: Boolean = false,
    val isAny: Boolean = false,
) : Node()

data class Shift(
    val from: State,
    val to: State,
    val functions: List<Function>
) : Node()

enum class StateKind {
    INIT, SIMPLE, FINISH;

    companion object {
        fun fromString(str: String) = when(str) {
            "finishstate" -> FINISH
            "state" -> SIMPLE
            "initstate" -> INIT
            else -> error("unknown state kind: $str")
        }
    }
}


data class Argument(
    val name: String,
    val type: Type
)

data class Function(
    val name: String,
    val automatonName: String?,
    val args: List<Argument>,
    val returnType: Type?,
    var contracts: List<Contract>,
    var statements: List<Statement>,
    val context: LslContext
) : Node()

sealed class Statement: Node()

data class Assignment(
    val variable: VariableAccess,
    val value: Expression
) : Statement()

data class Action(
    val name: String,
    val arguments: List<Expression>
) : Statement()

data class Contract(
    val name: String?,
    val expression: Expression,
    val kind: ContractKind
) : Node()

enum class ContractKind {
    REQUIRES, ENSURES
}

enum class AutomatonKind {
    REAL, SYNTHETIC
}

sealed class Expression: Node()

data class BinaryOpExpression(
    val left: Expression,
    val right: Expression,
    val op: ArithmeticBinaryOps
) : Expression()

enum class ArithmeticBinaryOps {
    ADD, SUB, MUL, DIV, AND, OR, XOR, MOD, EQ_EQ, NOT_EQ, GT, GT_EQ, LT, LT_EQ;
    companion object {
        fun fromString(str: String) = when (str) {
            "*" -> MUL
            "/" -> DIV
            "+" -> ADD
            "-" -> SUB
            "%" -> MOD
            "==" -> EQ_EQ
            "!=" -> NOT_EQ
            ">=" -> GT_EQ
            ">" -> GT
            "<=" -> LT_EQ
            "<" -> LT
            "&" -> AND
            "|" -> OR
            "^" -> XOR
            else -> error("unknown binary operator type")
        }
    }
}

data class UnaryOpExpression(
    val value: Expression,
    val op: ArithmeticUnaryOp
) : Expression()

enum class ArithmeticUnaryOp {
    MINUS, INVERSION
}

data class Variable(
    val name: String,
    val type: Type,
    val initValue: Expression?
) : Expression()

data class VariableAccess(
    val name: String,
    val automaton: Automaton,
    val arrayIndex: Int? = null
) : Expression()

data class IntegerNumber(
    val value: Int
) : Expression()

data class FloatNumber(
    val value: Float
) : Expression()

data class StringValue(
    val value: String
) : Expression()

data class OldValue(
    val value: VariableAccess
) : Expression()

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
    val extensionFunctions: Map<String, List<Function>>,
    val globalVariables: Map<String, GlobalVariableDeclaration>
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

data class SyntheticType(
    override val semanticType: String,
    override val realType: RealType,
    override val context: LslContext

) : Type()

data class Automaton(
    val name: String,
    val type: Type,
    var states: List<State>,
    var shifts: List<Shift>,
    var internalVariables: List<AutomatonVariableDeclaration>,
    var constructorVariables: List<Variable>,
    var localFunctions: List<Function>
) : Node() {
    val functions: List<Function>
        get() = localFunctions + (parent.node as Library).extensionFunctions[name].orEmpty()
    val variables: List<Variable>
        get() = internalVariables + constructorVariables
}

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

data class Function(
    val name: String,
    val automatonName: String,
    val args: List<FunctionArgument>,
    val returnType: Type?,
    var contracts: List<Contract>,
    var statements: List<Statement>,
    val context: LslContext
) : Node() {
    val automaton: Automaton by lazy { context.resolveAutomaton(automatonName) ?: error("unresolved automaton") }
}

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

sealed class Variable : Expression() {
    abstract val name: String
    abstract val type: Type
    open val initValue: Atomic? = null

    open val fullName: String
        get() = name
}

data class GlobalVariableDeclaration(
    override val name: String,
    override val type: Type,
    override val initValue: Atomic?
) : Variable()

data class AutomatonVariableDeclaration(
    override val name: String,
    override val type: Type,
    override var initValue: Atomic?
) : Variable() {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.${name}"
}

data class FunctionArgument(
    override val name: String,
    override val type: Type
) : Variable() {
    lateinit var function: Function

    override val fullName: String
        get() = "${function.name}.$name"
}

data class ConstructorArgument(
    override val name: String,
    override val type: Type,
) : Variable() {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.$name"
}

data class VariableAccess(
    val variable: Variable,
    val arrayIndex: Int? = null
) : Atomic()

sealed class Atomic : Expression()

data class IntegerNumber(
    val value: Int
) : Atomic()

data class FloatNumber(
    val value: Float
) : Atomic()

data class StringValue(
    val value: String
) : Atomic()

data class OldValue(
    val value: VariableAccess
) : Expression()

data class CallAutomatonConstructor(
    val automaton: Automaton,
    val args: List<ArgumentWithValue>,
    val state: State
) : Atomic()

data class ArgumentWithValue(
    val variable: Variable,
    val init: Atomic
)

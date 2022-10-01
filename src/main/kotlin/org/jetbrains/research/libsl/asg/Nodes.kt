package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.utils.IPrinter
import org.jetbrains.research.libsl.utils.IPrinter.Companion.SPACE
import org.jetbrains.research.libsl.visitors.addBacktickIfNeeded

sealed class Node {
    open val parent: NodeHolder = NodeHolder()
}

data class Library(
    val metadata: MetaNode,
    val imports: MutableList<String> = mutableListOf(),
    val includes: MutableList<String> = mutableListOf(),
    val semanticTypes: MutableList<Type> = mutableListOf(),
    val automata: MutableList<Automaton> = mutableListOf(),
    val extensionFunctions: MutableMap<String, MutableList<Function>> = mutableMapOf(),
    val globalVariables: MutableMap<String, GlobalVariableDeclaration> = mutableMapOf()
) : Node(), IPrinter {
    override fun dumpToString(): String = buildString {
        appendLine(metadata.dumpToString())
        append(formatImports())
        append(formatIncludes())
        append(formatTopLevelSemanticTypes())
        append(formatSemanticTypeBlock())
        append(formatGlobalVariables())
        append(formatAutomata())
    }

    private fun formatImports(): String {
        return simpleCollectionFormatter(imports, prefix = "import$SPACE", suffix = ";", addEmptyLastLine = true)
    }

    private fun formatIncludes(): String {
        return simpleCollectionFormatter(includes, prefix = "include$SPACE", suffix = ";", addEmptyLastLine = true)
    }

    private fun formatTopLevelSemanticTypes(): String {
        val topLevelTypes = semanticTypes.filter { type -> type.isTopLevelType }
        val formattedTypes = topLevelTypes.map { type -> type.dumpToString() }

        return simpleCollectionFormatter(collection = formattedTypes, suffix = "\n")
    }

    private fun formatSemanticTypeBlock(): String = buildString {
        val types = semanticTypes.filter { type -> type.isTypeBlockType }
        if (types.isEmpty())
            return@buildString

        appendLine("types {")
        append(withIndent(formatListEmptyLineAtEndIfNeeded(types)))
        appendLine("}")
    }

    private fun formatAutomata(): String = buildString {
        if (automata.isEmpty())
            return@buildString

        append(formatListEmptyLineAtEndIfNeeded(automata))
    }

    private fun formatGlobalVariables(): String = formatListEmptyLineAtEndIfNeeded(globalVariables.values.toList())
}

class MetaNode(
    var name: String,
    val libraryVersion: String? = null,
    val language: String? = null,
    var url: String? = null,
    val lslVersion: Triple<UInt, UInt, UInt>
) : Node(), IPrinter {
    val stringVersion: String
        get() {
            return "${lslVersion.first}.${lslVersion.second}.${lslVersion.third}"
        }

    // libsl "$libslVersion";
    // library $libraryName version "$libraryVersion" language "$language" url "libraryUrl"
    override fun dumpToString(): String = buildString {
        appendLine("libsl \"$stringVersion\";")
        append("library $name")

        if (libraryVersion != null) {
            append(SPACE + "version \"$libraryVersion\"")
        }

        if (language != null) {
            append(SPACE + "language \"$language\"")
        }

        if (url != null) {
            append(SPACE + "url \"$url\"")
        }
        appendLine(";")
    }
}

sealed interface Type : IPrinter {
    val name: String
    val isPointer: Boolean
    val context: LslContext
    val generic: Type?

    val fullName: String
        get() = "${if (isPointer) "*" else ""}$name${if (generic != null) "<${generic!!.fullName}>" else ""}"

    val isArray: Boolean
        get() = (this as? TypeAlias)?.originalType?.isArray == true || this is ArrayType

    val isTopLevelType: Boolean
        get() = false

    val isTypeBlockType: Boolean
        get() = false

    fun resolveFieldType(name: String): Type? {
        return when (this) {
            is EnumLikeSemanticType -> {
                this.entries.firstOrNull { it.first == name } ?: return null
                this.childrenType
            }
            is EnumType -> {
                this.entries.firstOrNull { it.first == name } ?: return null
                this.childrenType
            }
            is StructuredType -> {
                this.entries.firstOrNull { it.first == name }?.second
            }
            else -> null
        }
    }
}

sealed interface LibslType : Type

data class RealType (
    val nameParts: List<String>,
    override val isPointer: Boolean = false,
    override val generic: Type? = null,
    override val context: LslContext
) : Type {
    override val name: String
        get() = nameParts.joinToString(".")

    override fun toString(): String = "${if (isPointer) "*" else ""}$name${if (generic != null) "<${generic.fullName}>" else ""}"

    override fun dumpToString(): String {
        return toString()
    }
}

data class SimpleType(
    override val name: String,
    val realType: Type,
    override val isPointer: Boolean = false,
    override val context: LslContext
) : LibslType {
    override val generic: Type? = null
    override val isTypeBlockType: Boolean = true

    override fun dumpToString(): String {
        return "$name(${realType.dumpToString()});"
    }
}

sealed interface AliassableType : LibslType

data class TypeAlias (
    override val name: String,
    val originalType: AliassableType,
    override val context: LslContext
) : LibslType {
    override val isPointer: Boolean = false
    override val generic: Type? = null

    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String {
        return "typealias $name = ${originalType.fullName};"
    }
}

data class EnumLikeSemanticType(
    override val name: String,
    val type: Type,
    val entries: List<Pair<String, Atomic>>,
    override val context: LslContext
) : LibslType {
    override val isPointer: Boolean = false
    override val generic: Type? = null
    override val isTypeBlockType: Boolean = true

    val childrenType: Type = ChildrenType(name, context)

    override fun dumpToString(): String = buildString {
        appendLine("$name(${type.fullName}) {")
        val formattedEntries = entries.map { (k, v) -> "$k: ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }
}

class ChildrenType(
    override val name: String,
    override val context: LslContext,
) : Type {
    override val generic: Type? = null
    override val isPointer: Boolean = false

    override fun dumpToString(): String {
        error("unsupported operation exception")
    }
}

data class StructuredType(
    override val name: String,
    val type: Type,
    override val generic: Type? = null,
    val entries: List<Pair<String, Type>>,
    override val context: LslContext
) : AliassableType {
    override val isPointer: Boolean = false
    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String = buildString {
        appendLine("type ${type.fullName} {")
        val formattedEntries = entries.map { (k, v) -> "$k: ${v.fullName}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }
}

data class EnumType(
    override val name: String,
    val entries: List<Pair<String, Atomic>>,
    override val context: LslContext
) : AliassableType {
    override val isPointer: Boolean = false
    override val generic: Type? = null
    override val isTopLevelType: Boolean = true

    val childrenType: Type = ChildrenType(name, context)

    override fun dumpToString(): String = buildString {
        appendLine("enum $name {")
        val formattedEntries = entries.map { (k, v) -> "$k = ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }
}

data class ArrayType(
    override val name: String,
    override val isPointer: Boolean = false,
    override val generic: Type,
    override val context: LslContext
) :  AliassableType {
    override fun dumpToString(): String {
        return fullName
    }
}

data class Automaton(
    val name: String,
    val type: Type,
    var states: MutableList<State> = mutableListOf(),
    var shifts: MutableList<Shift> = mutableListOf(),
    var internalVariables: MutableList<AutomatonVariableDeclaration> = mutableListOf(),
    var constructorVariables: MutableList<ConstructorArgument> = mutableListOf(),
    var localFunctions: MutableList<Function> = mutableListOf()
) : Node(), IPrinter {
    val functions: List<Function>
        get() = localFunctions + (parent.node as Library).extensionFunctions[name].orEmpty()
    val variables: List<Variable>
        get() = internalVariables + constructorVariables

    override fun dumpToString(): String = buildString {
        append("automaton ${addBacktickIfNeeded(name)}")
        if (constructorVariables.isNotEmpty()) {
            append(" (${constructorVariables.joinToString(", ") { v -> v.dumpToString() } })")
        }
        appendLine(" : ${type.fullName} {")

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
}

data class State(
    val name: String,
    val kind: StateKind,
    val isSelf: Boolean = false,
    val isAny: Boolean = false,
) : Node(), IPrinter {
    lateinit var automaton: Automaton

    override fun dumpToString(): String = "${kind.keyword} $name;"
}

data class Shift(
    val from: State,
    val to: State,
    val functions: MutableList<Function> = mutableListOf()
) : Node(), IPrinter {
    override fun dumpToString(): String = buildString {
        append("shift ")
        append(from.name)
        append(" -> ")
        append(to.name)

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

data class Function(
    val name: String,
    val automatonName: String,
    var args: MutableList<FunctionArgument> = mutableListOf(),
    val returnType: Type?,
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    val hasBody: Boolean = statements.isNotEmpty(),
    var target: Automaton? = null,
    val context: LslContext
) : Node(), IPrinter {
    val automaton: Automaton by lazy { context.resolveAutomaton(automatonName) ?: error("unresolved automaton") }
    val qualifiedName: String
        get() = "${automaton.name}.$name"
    var resultVariable: Variable? = null

    override fun dumpToString(): String = buildString {
        append("fun ${addBacktickIfNeeded(name)}")

        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> buildString {
                if (arg.annotation != null) {
                    append("@")
                    append(arg.annotation!!.name)

                    if (arg.annotation!!.values.isNotEmpty()) {
                        append("(")
                        append(arg.annotation!!.values.joinToString(separator = ", ") { v ->
                            v.dumpToString()
                        })
                        append(")")
                    }

                    append(SPACE)
                }
                append(arg.name)
                append(": ")

                if (arg.annotation != null && arg.annotation is TargetAnnotation) {
                    append((arg.annotation as TargetAnnotation).targetAutomaton.name)
                } else {
                    append(arg.type.fullName)
                }
            } }
        )

        if (returnType != null) {
            append(": ")
            append(returnType.fullName)
        }

        if (contracts.isNotEmpty()) {
            appendLine()
            append(formatListEmptyLineAtEndIfNeeded(contracts))
        }

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else if (hasBody) {
            if (contracts.isEmpty()) {
                append(SPACE)
            }
            appendLine("{")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }
}

sealed class Statement: Node(), IPrinter

data class Assignment(
    val left: QualifiedAccess,
    val value: Expression
) : Statement() {
    override fun dumpToString(): String = "${left.dumpToString()} = ${value.dumpToString()};"
}

data class Action(
    val name: String,
    val arguments: MutableList<Expression> = mutableListOf()
) : Statement() {
    override fun dumpToString(): String = buildString {
        append("action ${addBacktickIfNeeded(name)}(")
        val args = arguments.map { it.dumpToString() }.toMutableList()
        append(args.joinToString(separator = ", "))
        append(");")
    }
}

data class Contract(
    val name: String?,
    val expression: Expression,
    val kind: ContractKind
) : Node(), IPrinter {
    override fun dumpToString(): String = buildString {
        append(kind.keyword)
        append(SPACE)
        if (name != null) {
            append(name)
            append(": ")
        }
        append(expression.dumpToString())
        append(";")
    }
}

enum class ContractKind(val keyword: String) {
    REQUIRES("requires"), ENSURES("ensures")
}

sealed class Expression: Node(), IPrinter

data class BinaryOpExpression(
    val left: Expression,
    val right: Expression,
    val op: ArithmeticBinaryOps
) : Expression() {
    override fun dumpToString(): String = "(${left.dumpToString()} ${op.string} ${right.dumpToString()})"
}

enum class ArithmeticBinaryOps(val string: String) {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), AND("&"),
    OR("|"), XOR("^"), MOD("%"), EQ("="), NOT_EQ("!="), GT(">"),
    GT_EQ(">="), LT("<"), LT_EQ("<=");
    companion object {
        fun fromString(str: String) = ArithmeticBinaryOps.values().first { op -> op.string == str }
    }
}

data class UnaryOpExpression(
    val value: Expression,
    val op: ArithmeticUnaryOp
) : Expression() {
    override fun dumpToString(): String = "${op.string}${value.dumpToString()}"
}

enum class ArithmeticUnaryOp(val string: String) {
    MINUS("-"), INVERSION("!")
}

sealed class Variable : Expression() {
    abstract val name: String
    abstract val type: Type
    open val initValue: Expression? = null

    open val fullName: String
        get() = name
}

data class GlobalVariableDeclaration(
    override val name: String,
    override val type: Type,
    override val initValue: Expression?
) : Variable() {
    override fun dumpToString(): String = buildString {
        append("var ${addBacktickIfNeeded(name)}: ${type.fullName}")
        if (initValue != null) {
            append(" = ${initValue.dumpToString()}")
        }

        appendLine()
    }
}

data class AutomatonVariableDeclaration(
    override val name: String,
    override val type: Type,
    override var initValue: Expression?
) : Variable() {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.${name}"

    override fun dumpToString(): String = buildString {
        append("var $name: ${type.fullName}")
        if (initValue != null) {
            append(" = ${initValue!!.dumpToString()}")
        }

        appendLine(";")
    }
}

data class FunctionArgument(
    override val name: String,
    override val type: Type,
    val index: Int,
    var annotation: Annotation? = null
) : Variable() {
    lateinit var function: Function

    override val fullName: String
        get() = "${function.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotation != null) {
            append("@${annotation!!.dumpToString()} ")
        }
        appendLine("$name: ${type.dumpToString()}")
    }
}

data class ResultVariable(
    override val type: Type
) : Variable() {
    override val name: String = "result"

    override fun dumpToString(): String = error("unsupported function call")
}

open class Annotation(
    val name: String,
    val values: MutableList<Expression> = mutableListOf()
) : IPrinter {
    override fun toString(): String {
        return "Annotation(name='$name', values=$values)"
    }

    override fun dumpToString(): String = buildString {
        append("@$name")
        if (values.isNotEmpty()) {
            append(values.joinToString(prefix = "(", postfix = ")", separator = ", ") { v -> v.dumpToString() })
        }

        appendLine()
    }
}

class TargetAnnotation(
    name: String,
    values: MutableList<Expression>,
    val targetAutomaton: Automaton
) : Annotation(name, values) {
    override fun toString(): String {
        return "TargetAnnotation(name='$name', values=$values, target=$targetAutomaton)"
    }
}

data class ConstructorArgument(
    override val name: String,
    override val type: Type,
) : Variable() {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.$name"

    override fun dumpToString(): String = "var $name: ${type.fullName}"
}

sealed class QualifiedAccess : Atomic() {
    abstract var childAccess: QualifiedAccess?
    abstract val type: Type

    override fun toString(): String = (childAccess?.toString() ?: "") + ":${type.fullName}"

    override val value: Any? = null

    val lastChild: QualifiedAccess
        get() = childAccess?.lastChild ?: childAccess ?: this
}

data class VariableAccess(
    val fieldName: String,
    override var childAccess: QualifiedAccess?,
    override val type: Type,
    val variable: Variable?
) : QualifiedAccess() {
    override fun toString(): String = "$fieldName${childAccess?.toString() ?: ""}"

    override fun dumpToString(): String = when {
        childAccess != null && childAccess is VariableAccess -> "$fieldName.${childAccess?.dumpToString()}"
        childAccess != null -> "$fieldName${childAccess?.dumpToString()}"
        else -> fieldName
    }
}

data class ArrayAccess(
    var index: Atomic,
    override val type: Type
) : QualifiedAccess() {
    override var childAccess: QualifiedAccess? = null

    override fun toString(): String = "${childAccess.toString()}[$index]"

    override fun dumpToString(): String = buildString {
        append("[${index.dumpToString()}]")
        if (childAccess != null) {
            append(childAccess!!.dumpToString())
        }
    }
}

data class AutomatonGetter(
    val automaton: Automaton,
    val arg: FunctionArgument,
    override var childAccess: QualifiedAccess?,
) : QualifiedAccess() {
    override val type: Type = automaton.type

    override fun toString(): String = "${automaton.name}(${arg.name}).${childAccess.toString()}"

    override fun dumpToString(): String = buildString {
        append(automaton.name)
        append("(")
        append(arg.name)
        append(")")

        if (childAccess != null) {
            append(".")
            append(childAccess!!.dumpToString())
        }
    }
}

data class OldValue(
    val value: QualifiedAccess
) : Expression() {
    override fun dumpToString(): String = "${value.dumpToString()}'"
}

data class CallAutomatonConstructor(
    val automaton: Automaton,
    val args: List<ArgumentWithValue>,
    val state: State
) : Atomic() {
    override val value: Any? = null

    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("new ${addBacktickIfNeeded(automaton.name)}")

        val formattedArgs = buildList {
            add("state = ${state.name}")
            for (arg in args) {
                add(arg.dumpToString())
            }
        }
        append(formattedArgs.joinToString(separator = ", ", prefix = "(", postfix = ")"))
    }
}

data class ArgumentWithValue(
    val variable: Variable,
    val init: Expression
) : IPrinter {
    override fun dumpToString(): String = "${variable.name} = ${init.dumpToString()}"
}

sealed class Atomic : Expression(), IPrinter {
    abstract val value: Any?

    override fun dumpToString(): String = value?.toString() ?: ""
}

data class IntegerLiteral(
    override val value: Int
) : Atomic()

data class FloatLiteral(
    override val value: Float
) : Atomic()

data class StringLiteral(
    override val value: String
) : Atomic() {
    override fun dumpToString(): String = "\"$value\""
}

data class BoolLiteral(
    override val value: Boolean
) : Atomic()

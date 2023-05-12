package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics

open class Function(
    open val kind: FunctionKind,
    open val name: String,
    open val automatonReference: AutomatonReference?,
    open var args: MutableList<FunctionArgument> = mutableListOf(),
    open val returnType: TypeReference?,
    open val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    open var localVariables: MutableList<Variable> = mutableListOf(),
    open var contracts: MutableList<Contract> = mutableListOf(),
    open var statements: MutableList<Statement> = mutableListOf(),
    open var hasBody: Boolean = statements.isNotEmpty(),
    open var targetAutomatonRef: AutomatonReference? = null,
    open val context: FunctionContext
) : Node() {
    val fullName: String
        get() = if(automatonReference?.name?.isEmpty() == true) "${automatonReference!!.name}.$name" else name

    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsages))
        append("${kind.value} ${BackticksPolitics.forIdentifier(name)}")
        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> arg.dumpToString() }
        )

        if (returnType != null) {
            append(": ")
            append(returnType!!.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)
        }

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else {
            appendLine(" {")
            hasBody = true

            if (contracts.isNotEmpty()) {
                append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
            }

            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }
}

enum class FunctionKind(val value: String) {
    FUNCTION("fun"), CONSTRUCTOR("constructor"), DESTRUCTOR("destructor"), PROC("proc");

    companion object {
        fun fromString(str: String) = FunctionKind.values().first { k -> k.value == str }
    }
}

data class Constructor(
    override val name: String,
    override var args: MutableList<FunctionArgument> = mutableListOf(),
    override val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override var contracts: MutableList<Contract> = mutableListOf(),
    override var statements: MutableList<Statement> = mutableListOf(),
    override var localVariables: MutableList<Variable> = mutableListOf(),
    override var hasBody: Boolean = statements.isNotEmpty(),
    override val context: FunctionContext
) : Function(
    kind = FunctionKind.CONSTRUCTOR, name, null, args, null,
    annotationUsages, localVariables, contracts,
    statements, hasBody, null, context
)

class Destructor(
    override val name: String,
    override var args: MutableList<FunctionArgument> = mutableListOf(),
    override val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override var contracts: MutableList<Contract> = mutableListOf(),
    override var statements: MutableList<Statement> = mutableListOf(),
    override var localVariables: MutableList<Variable> = mutableListOf(),
    override var hasBody: Boolean = statements.isNotEmpty(),
    override val context: FunctionContext
) : Function(
    kind = FunctionKind.DESTRUCTOR, name, null, args, null,
    annotationUsages, localVariables, contracts,
    statements, hasBody, null, context
)

class Procedure(
    override val name: String,
    override var args: MutableList<FunctionArgument> = mutableListOf(),
    override val returnType: TypeReference?,
    override val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    override var contracts: MutableList<Contract> = mutableListOf(),
    override var statements: MutableList<Statement> = mutableListOf(),
    override var localVariables: MutableList<Variable> = mutableListOf(),
    override var hasBody: Boolean = statements.isNotEmpty(),
    override val context: FunctionContext
) : Function(
    kind = FunctionKind.PROC, name, null, args, returnType,
    annotationUsages, localVariables, contracts,
    statements, hasBody, null, context
)

data class ArgumentWithValue(
    val name: String,
    val value: Expression
) : IPrinter {
    override fun dumpToString(): String = "${BackticksPolitics.forIdentifier(name)} = ${value.dumpToString()}"
}

package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.VariableReference
import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class QualifiedAccess : Atomic() {
    abstract var childAccess: QualifiedAccess?

    override fun toString(): String = dumpToString()

    override val value: Any? = null

    val lastChild: QualifiedAccess
        get() = childAccess?.lastChild ?: childAccess ?: this
}

/**
 * Access for variable.field[.childAccess]
 */
data class VariableAccess(
    val fieldName: String,
    override var childAccess: QualifiedAccess?,
    val variable: VariableReference
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()
    override fun dumpToString(): String = when {
        childAccess != null && childAccess is VariableAccess -> "${BackticksPolitics.forIdentifier(fieldName)}.${childAccess?.dumpToString()}"
        childAccess != null -> "${BackticksPolitics.forIdentifier(fieldName)}${childAccess?.dumpToString()}"
        else -> fieldName
    }
}

data class ArrayAccess(
    var index: Atomic,
) : QualifiedAccess() {
    override var childAccess: QualifiedAccess? = null

    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("[${index.dumpToString()}]")
        if (childAccess != null) {
            append(childAccess!!.dumpToString())
        }
    }
}

data class AutomatonOfFunctionArgumentInvoke(
    val automatonReference: AutomatonReference,
    val arg: FunctionArgument,
    override var childAccess: QualifiedAccess?,
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append(BackticksPolitics.forPeriodSeparated(automatonReference.name))
        append("(")
        append(BackticksPolitics.forIdentifier(arg.name))
        append(")")

        if (childAccess != null) {
            append(".")
            append(childAccess!!.dumpToString())
        }
    }
}
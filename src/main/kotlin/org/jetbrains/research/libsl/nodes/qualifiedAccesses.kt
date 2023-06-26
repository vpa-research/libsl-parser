package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.VariableReference
import org.jetbrains.research.libsl.utils.BackticksPolitics
import org.jetbrains.research.libsl.utils.EntityPosition

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
    val variable: VariableReference,
    val entityPosition: EntityPosition
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()
    override fun dumpToString(): String = when {
        childAccess != null && childAccess is VariableAccess -> "${BackticksPolitics.forIdentifier(fieldName)}.${childAccess?.dumpToString()}"
        childAccess != null -> "${BackticksPolitics.forIdentifier(fieldName)}${childAccess?.dumpToString()}"
        else -> fieldName
    }
}

data class ThisAccess(
    override var childAccess: QualifiedAccess?,
    val entityPosition: EntityPosition
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()
    override fun dumpToString(): String = buildString {
        append("this")
        if (childAccess != null) {
            append(".")
            append(childAccess!!.dumpToString())
        }
    }
}

data class ArrayAccess(
    var index: Atomic,
    val entityPosition: EntityPosition
) : QualifiedAccess() {
    override var childAccess: QualifiedAccess? = null
}

data class AutomatonOfFunctionArgumentInvoke(
    val automatonReference: AutomatonReference,
    val arg: FunctionArgument,
    override var childAccess: QualifiedAccess?,
    val entityPosition: EntityPosition
) : QualifiedAccess()

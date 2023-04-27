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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VariableAccess) return false

        if (fieldName != other.fieldName) return false
        if (childAccess != other.childAccess) return false
        if (variable != other.variable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fieldName.hashCode()
        result = 31 * result + (childAccess?.hashCode() ?: 0)
        result = 31 * result + variable.hashCode()
        return result
    }
}

data class ParentAccess(
    val parent: String = "parent",
    override var childAccess: QualifiedAccess?
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()
    override fun dumpToString(): String = buildString {
        append("PARENT USAGE")
    }
}

data class ProcAccess(
    val proc: Proc,
    override var childAccess: QualifiedAccess?,
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()
    override fun dumpToString(): String = buildString {
        append("PROC USAGE")
    }
}

data class ActionAccess(
    val action: Action,
    override var childAccess: QualifiedAccess?,
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()
    override fun dumpToString(): String = buildString {
        append("ACTION USAGE")
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
            if (childAccess is VariableAccess) {
                append(".")
            }

            append(childAccess!!.dumpToString())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArrayAccess) return false

        if (index != other.index) return false
        if (childAccess != other.childAccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + (childAccess?.hashCode() ?: 0)
        return result
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AutomatonOfFunctionArgumentInvoke) return false

        if (automatonReference != other.automatonReference) return false
        if (arg != other.arg) return false
        if (childAccess != other.childAccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = automatonReference.hashCode()
        result = 31 * result + arg.hashCode()
        result = 31 * result + (childAccess?.hashCode() ?: 0)
        return result
    }
}

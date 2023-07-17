package org.jetbrains.research.libsl.errors

import org.jetbrains.research.libsl.utils.EntityPosition
import org.jetbrains.research.libsl.utils.string

sealed interface LslError {
    val text: String
    val entityPosition: EntityPosition
}

typealias Position = Pair<Int, Int>

val Position.string: String
    get() = "$first:$second"

open class UnresolvedReference(
    override val text: String,
    override val entityPosition: EntityPosition,
    val kind: ReferenceKind
) : LslError {
    override fun toString(): String =
        "Unresolved reference of kind $kind with name $text in ${entityPosition.string}"
}

class UnresolvedVariable(
    text: String,
    position: EntityPosition
) : UnresolvedReference(text, position, ReferenceKind.Variable)


class UnresolvedFunction(
    text: String,
    position: EntityPosition
) : UnresolvedReference(text, position, ReferenceKind.Function)

class UnresolvedAutomaton(
    text: String,
    position: EntityPosition
) : UnresolvedReference(text, position, ReferenceKind.Automaton)

class UnresolvedState(
    text: String,
    position: EntityPosition
) : UnresolvedReference(text, position, ReferenceKind.State)

class UnresolvedType(
    text: String,
    position: EntityPosition
) : UnresolvedReference(text, position, ReferenceKind.Type)

class SemanticTypeExpected(
    text: String,
    position: EntityPosition
) : UnresolvedReference("expected semantic type but got $text", position, ReferenceKind.Type)


enum class ReferenceKind {
    Variable, Function, Automaton, State, Type
}

class UninitializedGlobalVariable(
    override val text: String,
    override val entityPosition: EntityPosition
) : LslError {
    override fun toString(): String =
        "Uninitialized global variable $text in ${entityPosition.string}"
}

class UnspecifiedAutomaton(
    override val text: String,
    override val entityPosition: EntityPosition
) : LslError {
    override fun toString(): String = "Unspecified automaton's name for function $text in ${entityPosition.string}"
}

class MoreThanOneTypesSection(
    override val entityPosition: EntityPosition
) : LslError {
    override val text: String = ""
    override fun toString(): String =
        "Only one `types` section must be provided. Another one provided at ${entityPosition.string}"
}

class UnresolvedImportOrInclude(
    override val text: String,
    override val entityPosition: EntityPosition
) : LslError {
    override fun toString(): String = "Unresolved import path $text on ${entityPosition.string}"
}

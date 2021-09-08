package org.jetbrains.research.libsl.errors

sealed interface LslError {
    val text: String
    val position: Position
}

typealias Position = Pair<Int, Int>

val Position.string: String
    get() = "$first:$second"

open class UnresolvedReference(
    override val text: String,
    override val position: Position,
    val kind: ReferenceKind
) : LslError {
    override fun toString(): String = "Unresolved reference of kind $kind with name $text on position ${position.string}"
}

class UnresolvedVariable(
    text: String,
    position: Position
) : UnresolvedReference(text, position, ReferenceKind.Variable)


class UnresolvedFunction(
    text: String,
    position: Position
) : UnresolvedReference(text, position, ReferenceKind.Function)

class UnresolvedAutomaton(
    text: String,
    position: Position
) : UnresolvedReference(text, position, ReferenceKind.Automaton)

class UnresolvedState(
    text: String,
    position: Position
) : UnresolvedReference(text, position, ReferenceKind.State)

class UnresolvedType(
    text: String,
    position: Position
) : UnresolvedReference(text, position, ReferenceKind.Type)


enum class ReferenceKind {
    Variable, Function, Automaton, State, Type
}

class UninitializedGlobalVariable(
    override val text: String,
    override val position: Position
) : LslError {
    override fun toString(): String =
        "Uninitialized global variable $text on ${position.string}. Uninitialized variables aren't allowed"
}

class UnspecifiedAutomaton(
    override val text: String,
    override val position: Position
) : LslError {
    override fun toString(): String = "Unspecified automaton's name for function $text on ${position.string}"
}

class MoreThanOneTypesSection(
    override val position: Position
) : LslError {
    override val text: String = ""
    override fun toString(): String = "Only one `types` section must be provided. Another one provided at ${position.string}"
}

class UnresolvedImportPath(
    override val text: String,
    override val position: Position
) : LslError {
    override fun toString(): String = "Unresolved import path $text on ${position.string}"
}
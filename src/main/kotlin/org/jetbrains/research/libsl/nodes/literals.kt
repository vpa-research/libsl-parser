package org.jetbrains.research.libsl.nodes

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
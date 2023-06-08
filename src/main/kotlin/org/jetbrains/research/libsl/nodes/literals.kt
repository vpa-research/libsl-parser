package org.jetbrains.research.libsl.nodes

data class IntegerLiteral(
    override val value: Int
) : Atomic()

data class FloatLiteral(
    override val value: Float,
    val suffix: String
) : Atomic()

data class StringLiteral(
    override val value: String
) : Atomic()

data class BoolLiteral(
    override val value: Boolean
) : Atomic()

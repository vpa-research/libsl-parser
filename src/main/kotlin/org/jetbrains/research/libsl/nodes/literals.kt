package org.jetbrains.research.libsl.nodes

data class IntegerLiteral(
    override val value: Int
) : Atomic()

abstract class FloatLiteral(
    override val value: Float,
    open val suffix: String
) : Atomic()

data class Float32Literal(
    override val value: Float,
    override val suffix: String = "f"
) : FloatLiteral(value, suffix)

data class Float64Literal(
    override val value: Float,
    override val suffix: String = "d"
) : FloatLiteral(value, suffix)

data class StringLiteral(
    override val value: String
) : Atomic()

data class BoolLiteral(
    override val value: Boolean
) : Atomic()

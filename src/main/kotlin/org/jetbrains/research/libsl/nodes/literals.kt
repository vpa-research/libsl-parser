package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.utils.EntityPosition

data class IntegerLiteral(
    override val value: Int,
    val entityPosition: EntityPosition
) : Atomic()

abstract class FloatLiteral(
    override val value: Any,
    open val suffix: String,
    open val entityPosition: EntityPosition
) : Atomic()

data class Float32Literal(
    override val value: Float,
    override val suffix: String = "f",
    override val entityPosition: EntityPosition
) : FloatLiteral(value, suffix, entityPosition)

data class Float64Literal(
    override val value: Double,
    override val suffix: String = "d",
    override val entityPosition: EntityPosition
) : FloatLiteral(value, suffix, entityPosition)

data class StringLiteral(
    override val value: String,
    val entityPosition: EntityPosition
) : Atomic()

data class BoolLiteral(
    override val value: Boolean,
    val entityPosition: EntityPosition
) : Atomic()

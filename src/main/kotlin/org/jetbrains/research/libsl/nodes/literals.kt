package org.jetbrains.research.libsl.nodes

sealed class LiteralWithSuffix : Atomic() {
    abstract override val value: Any?
    abstract val suffix: String?
}
sealed class NumberLiteral<T> : LiteralWithSuffix() {
    abstract override val value: T
    abstract override val suffix: String?
}

data class IntegerLiteral(
    override val value: Number,
    override val suffix: String?
) : NumberLiteral<Number>()

data class UnsignedInt8Literal(
    override val value: UByte,
    override val suffix: String?
) : NumberLiteral<UByte>()

data class UnsignedInt16Literal(
    override val value: UShort,
    override val suffix: String?
) : NumberLiteral<UShort>()

data class UnsignedInt32Literal(
    override val value: UInt,
    override val suffix: String?
) : NumberLiteral<UInt>()

data class UnsignedInt64Literal(
    override val value: ULong,
    override val suffix: String?
) : NumberLiteral<ULong>()

data class FloatLiteral(
    override val value: Number,
    override val suffix: String?
) : NumberLiteral<Number>()

data class StringLiteral(
    override val value: String
) : Atomic()

data class BoolLiteral(
    override val value: Boolean
) : Atomic()

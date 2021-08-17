package org.jetbrains.research.libsl.asg

interface PrimitiveType : Type {
    override val isPointer: Boolean
        get() = false
}

data class IntType(
    override val context: LslContext,
    val capacity: IntCapacity
) : PrimitiveType {
    override val generic: Type? = null
    override val name: String = capacity.name.lowercase()

    enum class IntCapacity {
        INT8, INT16, INT32, INT64
    }
}

data class UnsignedType(
    override val context: LslContext,
    val capacity: UnsignedCapacity
) : PrimitiveType {
    override val generic: Type? = null
    override val name: String = capacity.name.lowercase()

    enum class UnsignedCapacity {
        UNSIGNED8, UNSIGNED16, UNSIGNED32, UNSIGNED64
    }
}

data class FloatType(
    override val context: LslContext,
    val capacity: FloatCapacity
) : PrimitiveType {
    override val generic: Type? = null
    override val name: String = capacity.name.lowercase()

    enum class FloatCapacity {
        FLOAT32, FLOAT64
    }
}

data class BoolType(
    override val context: LslContext,
) : PrimitiveType {
    override val generic: Type? = null
    override val name: String = "bool"
}

data class CharType(
    override val context: LslContext
) : PrimitiveType {
    override val generic: Type? = null
    override val name: String = "char"
}

data class StringType(
    override val context: LslContext
) : PrimitiveType {
    override val generic: Type? = null
    override val name: String = "string"
}

sealed class Atomic : Expression() {
    abstract val value: Any?
}

data class IntegerNumber(
    override val value: Int
) : Atomic()

data class FloatNumber(
    override val value: Float
) : Atomic()

data class StringValue(
    override val value: String
) : Atomic()

data class Bool(
    override val value: Boolean
) : Atomic()
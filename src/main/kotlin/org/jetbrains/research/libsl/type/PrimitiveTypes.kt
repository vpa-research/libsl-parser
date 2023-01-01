package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.TypeReference

interface PrimitiveType : Type {
    override fun dumpToString(): String {
        return fullName
    }
}

data class IntType(
    override val context: LslContextBase,
    val capacity: IntCapacity,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generic: TypeReference? = null
    override val name: String = capacity.name.lowercase()

    enum class IntCapacity {
        INT8, INT16, INT32, INT64, UNKNOWN
    }
}

data class UnsignedType(
    override val context: LslContextBase,
    val capacity: UnsignedCapacity,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generic: TypeReference? = null
    override val name: String = capacity.name.lowercase()

    enum class UnsignedCapacity {
        UNSIGNED8, UNSIGNED16, UNSIGNED32, UNSIGNED64, UNKNOWN
    }
}

data class FloatType(
    override val context: LslContextBase,
    val capacity: FloatCapacity,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generic: TypeReference? = null
    override val name: String = capacity.name.lowercase()

    enum class FloatCapacity {
        FLOAT32, FLOAT64, UNKNOWN
    }
}

data class BoolType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generic: TypeReference? = null
    override val name: String = "bool"
}

data class CharType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generic: TypeReference? = null
    override val name: String = "char"
}

data class StringType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generic: TypeReference? = null
    override val name: String = "string"
}

data class VoidType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val name: String = "void"
    override val generic: TypeReference? = null
}

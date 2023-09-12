package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder

interface PrimitiveType : Type {
    override fun dumpToString(): String {
        return fullName
    }
}

data class Int8Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "int8"
}

data class Int16Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "int16"
}

data class Int32Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "int32"
}

data class Int64Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "int64"
}

data class UnsignedInt8Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "unsigned8"
}

data class UnsignedInt16Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "unsigned16"
}

data class UnsignedInt32Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "unsigned32"
}

data class UnsignedInt64Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "unsigned64"
}

data class Float32Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "float32"
}

data class Float64Type(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "float64"
}

data class BoolType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "bool"
}

data class CharType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "char"
}

data class StringType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val generics: MutableList<TypeReference> = mutableListOf()
    override val name: String = "string"
}

data class VoidType(
    override val context: LslContextBase,
    override val isPointer: Boolean = false
) : PrimitiveType {
    override val name: String = "void"
    override val generics: MutableList<TypeReference> = mutableListOf()
}

data class AnyType(
    override val context: LslContextBase
) : PrimitiveType {
    override val name: String = ANY_TYPE_NAME
    override val isPointer: Boolean = false
    override val generics: MutableList<TypeReference> = mutableListOf()

    companion object {
        const val ANY_TYPE_NAME = "any"

        fun getAnyTypeReference(context: LslContextBase): TypeReference{
            return TypeReferenceBuilder.build(ANY_TYPE_NAME, genericReferences = mutableListOf(), isPointer = false, context)
        }
    }
}

data class NothingType(
    override val context: LslContextBase
) : PrimitiveType {
    override val name: String = Nothing_TYPE_NAME
    override val isPointer: Boolean = false
    override val generics: MutableList<TypeReference> = mutableListOf()
    companion object {
        const val Nothing_TYPE_NAME = "nothing"

        fun getNothingTypeReference(context: LslContextBase): TypeReference{
            return TypeReferenceBuilder.build(Nothing_TYPE_NAME, genericReferences = mutableListOf(), isPointer = false, context)
        }
    }
}

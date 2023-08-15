package org.jetbrains.research.libsl.utils

import org.jetbrains.research.libsl.type.*

object QualifiedAccessUtils {
    fun resolveFieldType(parentType: Type, name: String): Type? {

        return when (parentType) {
            is StructuredType -> parentType.variables.firstOrNull {it.name == name}?.typeReference?.resolve()
            is ArrayType -> parentType.generics.firstOrNull()?.resolve()?.let { resolveFieldType(it, name) }
            is ListType -> parentType.generics.firstOrNull()?.resolve()?.let { resolveFieldType(it, name) }
            is EnumType -> IntType(parentType.context, IntType.IntCapacity.UNKNOWN)
            is EnumLikeSemanticType -> IntType(parentType.context, IntType.IntCapacity.UNKNOWN)
            is TypeAlias -> parentType.originalType.resolve()?.let { resolveFieldType(it, name) }

            is NullType -> null
            is SimpleType -> null
            is PrimitiveType -> null
            is RealType -> null
        }
    }
}

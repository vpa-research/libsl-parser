package org.jetbrains.research.libsl.utils

import org.jetbrains.research.libsl.type.*

object QualifiedAccessUtils {
    fun resolveFieldType(parentType: Type, name: String): Type? {
        return when (parentType) {
            is StructuredType -> parentType.entries.entries.firstOrNull { it.key == name }?.value?.resolve()

            is ArrayType -> parentType.generic.resolve()?.let { resolveFieldType(it, name) }
            is EnumType -> IntType(parentType.context, IntType.IntCapacity.UNKNOWN)
            is EnumLikeSemanticType -> IntType(parentType.context, IntType.IntCapacity.UNKNOWN)
            is TypeAlias -> parentType.originalType.resolve()?.let { resolveFieldType(it, name) }

            is SimpleType -> null
            is PrimitiveType -> null
            is RealType -> null
        }
    }
}

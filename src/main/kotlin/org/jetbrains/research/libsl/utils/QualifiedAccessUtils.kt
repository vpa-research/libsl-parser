package org.jetbrains.research.libsl.utils

import org.jetbrains.research.libsl.type.*

object QualifiedAccessUtils {
    fun resolveFieldType(parentType: Type, name: String): Type? {
        return when (parentType) {
            is StructuredType -> parentType.entries.entries.firstOrNull { it.key == name }?.value?.resolve()
            is ArrayType -> parentType.generic.resolve()?.let { resolveFieldType(it, name) }
            is EnumType -> Int32Type(parentType.context)
            is EnumLikeSemanticType -> Int32Type(parentType.context)
            is TypeAlias -> parentType.originalType.resolve()?.let { resolveFieldType(it, name) }

            is SimpleType -> null
            is PrimitiveType -> null
            is RealType -> null
        }
    }
}

package org.jetbrains.research.libsl.utils
//
//import org.jetbrains.research.libsl.nodes.*
//import org.jetbrains.research.libsl.type.*
//
//object QualifiedAccessUtils {
//    /**
//     * resolves an access chain like foo.bar.tar by [names] chain and [parentType]
//     * @param throwExceptions: if true an exception will be thrown on any unresolved name in chain,
//     * otherwise an error will be ignored and only resolved part of chain will be returned
//     */
//    @Suppress("MemberVisibilityCanBePrivate", "unused")
//    fun resolvePeriodSeparatedChain(
//        parentType: Type,
//        names: List<String>,
//        throwExceptions: Boolean = true
//    ): QualifiedAccess? {
//        val name = names.firstOrNull() ?: return null
//
//        val access = when(parentType) {
//            is StructuredType -> {
//                val entry = parentType.entries.entries.firstOrNull { it.key == name }
//                    ?: if (throwExceptions) {
//                        error("unresolved field $name in type ${parentType.name}")
//                    } else {
//                        return null
//                    }
//
//                VariableAccess(
//                    name,
//                    if (names.size > 1) {
//                        resolvePeriodSeparatedChain(entry.value.resolveOrError(), names.drop(1), throwExceptions)
//                    } else{
//                        null
//                    },
//                    entry.value.resolveOrError(),
//                    null
//                )
//            }
//
//            is EnumType -> {
//                val entry = parentType.entries.entries.firstOrNull { it.key == name }
//                    ?: if (throwExceptions) {
//                        error("unresolved field $name in type ${parentType.name}")
//                    } else {
//                        return null
//                    }
//
//                VariableAccess(
//                    entry.key,
//                    null,
//                    parentType,
//                    null
//                )
//            }
//
//            is EnumLikeSemanticType -> {
//                val entry = parentType.entries.entries.firstOrNull { it.key == name }
//                    ?: if (throwExceptions) {
//                        error("unresolved field $name in type ${parentType.name}")
//                    } else {
//                        return null
//                    }
//                VariableAccess(
//                    entry.key,
//                    null,
//                    parentType,
//                    null
//                )
//            }
//
//            is TypeAlias -> {
//                return resolvePeriodSeparatedChain(parentType.originalType, names, throwExceptions)
//            }
//
//            else -> {
//                if (names.size == 1) {
//                    if (parentType !is FieldTypedType) {
//                        if (throwExceptions) {
//                            error("can't resolve chain for $parentType")
//                        } else {
//                            return null
//                        }
//                    }
//
//                    val fieldType = parentType.getFieldTypeReference(name)
//                        ?: if (throwExceptions) {
//                            error("unresolved field $name in type ${parentType.name}")
//                        } else {
//                            return null
//                        }
//
//                    VariableAccess(
//                        name,
//                        null,
//                        fieldType,
//                        null
//                    )
//                } else {
//                    if (throwExceptions) {
//                        error("can't resolve access chain. Unsupported part type: ${parentType::class.java}")
//                    }
//
//                    return null
//                }
//            }
//        }
//
//        if (names.size == 1)
//            return access
//
//        return access.apply {
//            this.childAccess = resolvePeriodSeparatedChain(this.typeReference, names.drop(1), throwExceptions)
//        }
//    }
//}
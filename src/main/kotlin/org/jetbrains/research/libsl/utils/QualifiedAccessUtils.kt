package org.jetbrains.research.libsl.utils

import org.jetbrains.research.libsl.asg.*

object QualifiedAccessUtils {
    /**
     * resolves an access chain like foo.bar.tar by [names] chain and [parentType]
     * @param throwExceptions: if true an exception will be thrown on any unresolved name in chain,
     * otherwise an error will be ignored and only resolved part of chain will be returned
     */
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun resolvePeriodSeparatedChain(
        parentType: Type,
        names: List<String>,
        throwExceptions: Boolean = true
    ): QualifiedAccess? {
        val name = names.firstOrNull() ?: return null

        val access = when(parentType) {
            is StructuredType -> {
                val entry = parentType.entries.entries.firstOrNull { it.key == name }
                    ?: if (throwExceptions) {
                        error("unresolved field $name in type ${parentType.name}")
                    } else {
                        return null
                    }

                VariableAccess(
                    name,
                    if (names.size > 1) resolvePeriodSeparatedChain(entry.value, names.drop(1)) else null,
                    entry.value,
                    null
                )
            }

            is EnumType -> {
                val entry = parentType.entries.entries.firstOrNull { it.key == name }
                    ?: if (throwExceptions) {
                        error("unresolved field $name in type ${parentType.name}")
                    } else {
                        return null
                    }

                VariableAccess(
                    entry.key,
                    null,
                    parentType,
                    null
                )
            }

            is EnumLikeSemanticType -> {
                val entry = parentType.entries.entries.firstOrNull { it.key == name }
                    ?: if (throwExceptions) {
                        error("unresolved field $name in type ${parentType.name}")
                    } else {
                        return null
                    }
                VariableAccess(
                    entry.key,
                    null,
                    parentType,
                    null
                )
            }

            else -> {
                if (names.size == 1) {

                    if (parentType !is FieldTypedType) {
                        if (throwExceptions) {
                            error("can't resolve chain for $parentType")
                        } else {
                            return null
                        }
                    }

                    val fieldType = parentType.getFieldType(name)
                        ?: if (throwExceptions) {
                            error("unresolved field $name in type ${parentType.name}")
                        } else {
                            return null
                        }

                    VariableAccess(
                        name,
                        null,
                        fieldType,
                        null
                    )
                } else {
                    error("can't resolve access chain. Unsupported part type: ${parentType::class.java}")
                }
            }
        }

        if (names.size == 1)
            return access

        return access.apply {
            this.childAccess = resolvePeriodSeparatedChain(this.type, names.drop(1))
        }
    }
}
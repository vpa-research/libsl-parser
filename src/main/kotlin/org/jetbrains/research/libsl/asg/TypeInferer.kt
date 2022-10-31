package org.jetbrains.research.libsl.asg

class TypeInferer(private val context: LslContext) {
    @Suppress("MemberVisibilityCanBePrivate", "unused")
    fun getExpressionTypeOrNull(expression: Expression): Type? {
        return try {
            getExpressionType(expression)
        } catch (e: Exception) {
            null
        }
    }

    fun getExpressionType(expression: Expression): Type {
        return when (expression) {
            is Atomic -> getAtomicType(expression)
            is BinaryOpExpression -> {
                val typeA = getExpressionType(expression.left)
                val typeB = getExpressionType(expression.right)
                mergeTypes(typeA, typeB)
            }
            is OldValue -> expression.value.type
            is UnaryOpExpression -> getExpressionType(expression.value)
            is Variable -> expression.type
        }
    }

    private fun getAtomicType(atomic: Atomic): Type {
        return when (atomic) {
            is BoolLiteral -> BoolType(context)
            is FloatLiteral -> FloatType(context, FloatType.FloatCapacity.UNKNOWN)
            is IntegerLiteral -> IntType(context, IntType.IntCapacity.UNKNOWN)
            is StringLiteral -> StringType(context)
            is CallAutomatonConstructor -> atomic.automaton.type
            is QualifiedAccess -> atomic.type
        }
    }

    fun mergeTypesOrNull(typeA: Type, typeB: Type): Type? {
        return try {
            mergeTypes(typeA, typeB)
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun mergeTypes(typeA: Type, typeB: Type): Type {
        check(typeA::class == typeB::class) { "Unsupported merge for types: $typeA & $typeB" }

        if (typeA is IntType) {
            typeB as IntType
            check(typeA.capacity == typeB.capacity) { "Capacities not mach: ${typeA.capacity} & ${typeB.capacity}" }
        }

        if (typeA is FloatType) {
            typeB as FloatType
            check(typeA.capacity == typeB.capacity) { "Capacities not mach: ${typeA.capacity} & ${typeB.capacity}" }
        }

        if (typeA is UnsignedType) {
            typeB as UnsignedType
            check(typeA.capacity == typeB.capacity) { "Capacities not mach: ${typeA.capacity} & ${typeB.capacity}" }
        }

        return typeA
    }
}

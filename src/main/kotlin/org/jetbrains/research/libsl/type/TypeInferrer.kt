package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference

class TypeInferrer(private val context: LslContextBase) {
    private val anyType by lazy { context.resolveType(AnyType.getAnyTypeReference(context))!! }
    private val nothingType by lazy { context.resolveType(NothingType.getNothingTypeReference(context))!! }
    private val conceptType by lazy {context.resolveType(ConceptType.getConceptTypeReference(context)!!)}

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
            is UnaryOpExpression -> getExpressionType(expression.value)
            is Variable -> expression.typeReference.resolveOrError()
            is OldValue -> getExpressionType(expression.value)
            is HasAutomatonConcept -> BoolType(context)

            // TODO("Action type")
            is ActionExpression -> anyType
            is ProcExpression -> anyType
        }
    }

    private fun getAtomicType(atomic: Atomic): Type {
        return when (atomic) {
            is BoolLiteral -> BoolType(context)
            is FloatLiteral -> FloatType(context, FloatType.FloatCapacity.UNKNOWN)
            is IntegerLiteral -> IntType(context, IntType.IntCapacity.UNKNOWN)
            is StringLiteral -> StringType(context)
            is CallAutomatonConstructor -> atomic.automatonRef.resolveOrError().typeReference!!.resolveOrError()
            is QualifiedAccess -> getQualifiedAccessType(atomic)
            is ArrayLiteral -> getArrayLiteralType(atomic)
        }
    }

    private fun getQualifiedAccessType(access: QualifiedAccess): Type {
        return when (access) {
            is ArrayAccess -> TODO()
            is AutomatonOfFunctionArgumentInvoke -> access.automatonReference.resolveOrError().typeReference.resolveOrError()
            is VariableAccess -> access.variable.resolveOrError().typeReference.resolveOrError()
            is ThisAccess -> getQualifiedAccessType(access.lastChild)
        }
    }

    private fun getArrayLiteralType(arrayLiteral: ArrayLiteral): Type {
        val typeOfElements = arrayLiteral.value.fold(nothingType) { acc, expression ->
            mergeTypes(acc, getExpressionType(expression))
        }

        return ArrayType(
            isPointer = false,
            generic = typeOfElements.getReference(context),
            context = context
        )
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

        if (typeA::class == typeB::class) {
            return typeA
        }

        if (typeA is NothingType) {
            return typeB
        }

        if (typeB is NothingType) {
            return typeA
        }

        return anyType
    }
}

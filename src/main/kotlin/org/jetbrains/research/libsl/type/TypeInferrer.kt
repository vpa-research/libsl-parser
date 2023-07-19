package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference

class TypeInferrer(private val context: LslContextBase) {
    private val anyType by lazy { context.resolveType(AnyType.getAnyTypeReference(context))!! }
    private val nothingType by lazy { context.resolveType(NothingType.getNothingTypeReference(context))!! }

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
            is HasAutomaton -> BoolType(context)
            is NamedArgumentWithValue -> getExpressionType(expression.value)
            is ActionExpression -> expression.actionUsage.actionReference.resolveOrError().returnType?.resolveOrError()
                ?: VoidType(context)
            is ProcExpression -> expression.procedureCall.procReference.resolveOrError().returnType?.resolveOrError()
                ?: VoidType(context)
        }
    }

    private fun getAtomicType(atomic: Atomic): Type {
        return when (atomic) {
            is BoolLiteral -> BoolType(context)
            is FloatLiteral -> processFloatLiteralType(atomic, context)
            is IntegerLiteral -> processIntegerLiteralType(atomic, context)
            is StringLiteral -> StringType(context)
            is CallAutomatonConstructor -> atomic.automatonRef.resolveOrError().typeReference.resolveOrError()
            is QualifiedAccess -> getQualifiedAccessType(atomic)
            is ArrayLiteral -> getArrayLiteralType(atomic)
        }
    }

    private fun processIntegerLiteralType(integerLiteral: IntegerLiteral, context: LslContextBase): Type {
        return when(integerLiteral.suffix) {
            "b" -> Int8Type(context)
            "ub" -> UnsignedInt8Type(context)
            "s" -> Int16Type(context)
            "us" -> UnsignedInt16Type(context)
            null -> Int32Type(context)
            "u" -> UnsignedInt32Type(context)
            "L" -> Int64Type(context)
            "uL" -> UnsignedInt64Type(context)
            else -> error("Unknown integer literal, no such type")
        }
    }

    private fun processFloatLiteralType(floatLiteral: FloatLiteral, context: LslContextBase): Type {
        return when(floatLiteral.suffix) {
            "f" -> Float32Type(context)
            null -> Float64Type(context)
            else -> throw IllegalArgumentException("Unknown float literal, no such type")
        }
    }

    private fun getQualifiedAccessType(access: QualifiedAccess): Type {
        return when (access) {
            is ArrayAccess -> anyType
            is AutomatonOfFunctionArgumentInvoke -> access.automatonReference.resolveOrError().typeReference.resolveOrError()
            is VariableAccess -> access.variable.resolveOrError().typeReference.resolveOrError()
            is ThisAccess -> anyType
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

package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.utils.PositionGetter

class TypeInferrer(private val context: LslContextBase) {
    private val anyType by lazy { context.resolveType(AnyType.getAnyTypeReference(context))!! }
    private val nothingType by lazy { context.resolveType(NothingType.getNothingTypeReference(context))!! }
    private val fileName = context.fileName
    private val posGetter = PositionGetter()

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
            is NamedArgumentWithValue -> getExpressionType(expression.value)
            is TypeOperationExpression -> getExpressionType(expression.expression)
            is ActionExpression -> expression.actionUsage.actionReference.resolveOrError().returnType?.resolveOrError()
                ?: VoidType(context)
            // is ProcExpression -> expression.procedureCall.procReference.resolveOrError().returnType?.resolveOrError()
            // ?: VoidType(context)
            is ProcExpression -> VoidType(context)
            is FunctionUsageExpression -> expression.functionUsage.functionReference.resolveOrError().returnType?.resolveOrError()
                ?: VoidType(context)
        }
    }

    private fun getAtomicType(atomic: Atomic): Type {
        return when (atomic) {
            is BoolLiteral -> BoolType(context)
            is NullLiteral -> NullType(false, mutableListOf(), context)
            is FloatLiteral -> processFloatLiteralType(atomic, context)
            is IntegerLiteral -> processIntegerLiteralType(atomic, context)
            is UnsignedInt16Literal -> processIntegerLiteralType(atomic, context)
            is UnsignedInt32Literal -> processIntegerLiteralType(atomic, context)
            is UnsignedInt64Literal -> processIntegerLiteralType(atomic, context)
            is UnsignedInt8Literal -> processIntegerLiteralType(atomic, context)
            is StringLiteral -> StringType(context)
            is CharacterLiteral -> CharType(context)
            is CallAutomatonConstructor -> atomic.automatonRef.resolveOrError().typeReference.resolveOrError()
            is QualifiedAccess -> getQualifiedAccessType(atomic)
            is ArrayLiteral -> getArrayLiteralType(atomic)
        }
    }

    private fun processIntegerLiteralType(literalWithSuffix: LiteralWithSuffix, context: LslContextBase): Type {
        return when(literalWithSuffix.suffix) {
            "b" -> Int8Type(context)
            "ub" -> UnsignedInt8Type(context)
            "s" -> Int16Type(context)
            "us" -> UnsignedInt16Type(context)
            null -> Int32Type(context)
            "u" -> UnsignedInt32Type(context)
            "L" -> Int64Type(context)
            "uL" -> UnsignedInt64Type(context)
            else -> throw IllegalArgumentException("Unknown integer literal, no such type")
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
            is ArrayAccess -> AnyType(context)
            is AutomatonVariableInvoke -> AnyType(context)
            is AutomatonProcedureCall -> AnyType(context)
            is VariableAccess -> AnyType(context)
            is ThisAccess -> AnyType(context)
        }
    }

    private fun getArrayLiteralType(arrayLiteral: ArrayLiteral): Type {
        val typeOfElements = arrayLiteral.value.fold(nothingType) { acc, expression ->
            mergeTypes(acc, getExpressionType(expression))
        }

        return ArrayType(
            isPointer = false,
            generics = mutableListOf(typeOfElements.getReference(context)),
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

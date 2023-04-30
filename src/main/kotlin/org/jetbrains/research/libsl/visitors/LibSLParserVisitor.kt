package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.TypeIdentifierContext
import org.jetbrains.research.libsl.LibSLParserBaseVisitor
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.AnnotationUsage
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.builders.AnnotationReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.type.ArrayType
import org.jetbrains.research.libsl.type.RealType
import org.jetbrains.research.libsl.type.Type

abstract class LibSLParserVisitor<T>(val context: LslContextBase) : LibSLParserBaseVisitor<T>() {
    protected fun processTypeIdentifier(ctx: TypeIdentifierContext): TypeReference {
        val typeName = ctx.name.asPeriodSeparatedString()
        val isPointer = ctx.asterisk != null
        val genericTypeIdentifierContext = ctx.generic

        val generic = genericTypeIdentifierContext?.let { genericCtx -> getRealType(genericCtx) }
        val genericReference = generic?.getReference(context)

        return TypeReferenceBuilder.build(typeName, genericReference, isPointer, context)
    }

    private fun getRealType(ctx: TypeIdentifierContext): RealType {
        val typeNameParts = ctx.name.asPeriodSeparatedParts()
        val isPointer = ctx.asterisk != null
        val genericTypeIdentifierContext = ctx.generic

        val generic = genericTypeIdentifierContext?.let { genericCtx -> getRealType(genericCtx) }

        val realType = RealType(typeNameParts, isPointer, generic?.getReference(context), context)

        val previouslyStoredType = context.resolveType(realType.getReference(context))
        if (previouslyStoredType != null && previouslyStoredType is RealType) {
            return previouslyStoredType
        }

        context.storeType(realType)
        return realType
    }

    private fun getArrayType(ctx: TypeIdentifierContext): ArrayType {
        val typeNameParts = ctx.name.asPeriodSeparatedParts()
        check(typeNameParts[0] == "array" && typeNameParts.size == 1) { "not an array" }

        val isPointer = ctx.asterisk != null
        val genericTypeIdentifierContext = ctx.generic

        val generic = genericTypeIdentifierContext?.let { genericCtx -> getRealType(genericCtx) }
        check(generic != null)

        return ArrayType(isPointer, generic.getReference(context), context)
    }

    fun getRealTypeOrArray(ctx: TypeIdentifierContext): Type {
        val typeNameParts = ctx.name.asPeriodSeparatedParts()

        return if (typeNameParts[0] == "array") {
            getArrayType(ctx)
        } else {
            getRealType(ctx)
        }
    }

    protected fun getAnnotationUsages(ctx: List<LibSLParser.AnnotationUsageContext>): MutableList<AnnotationUsage> {
        return ctx.map { processAnnotationUsage(it) }.toMutableList()
    }

    private fun processAnnotationUsage(ctx: LibSLParser.AnnotationUsageContext): AnnotationUsage {
        val name = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val args = ctx.expressionsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }.orEmpty().toMutableList()

        val argTypes = args.map { argument -> context.typeInferrer.getExpressionType(argument).getReference(context) }
        val annotationRef = AnnotationReferenceBuilder.build(name, argTypes, context)

        return AnnotationUsage(annotationRef, args)
    }
}

package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.TypeIdentifierContext
import org.jetbrains.research.libsl.LibSLParserBaseVisitor
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.AnnotationUsage
import org.jetbrains.research.libsl.nodes.Generic
import org.jetbrains.research.libsl.nodes.GenericModifier
import org.jetbrains.research.libsl.nodes.NamedArgumentWithValue
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.builders.AnnotationReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.type.ArrayType
import org.jetbrains.research.libsl.type.RealType
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.PositionGetter

abstract class LibSLParserVisitor<T>(open val context: LslContextBase) : LibSLParserBaseVisitor<T>() {

    private val posGetter = PositionGetter()

    internal fun processTypeIdentifier(ctx: TypeIdentifierContext): TypeReference {
        val typeName = ctx.name.asPeriodSeparatedString()
        val isPointer = ctx.asterisk != null
        val generics = mutableListOf<Generic>()

        if(ctx.generic() != null) {
            generics.addAll(processGenerics(ctx.generic()))
        }

        return TypeReferenceBuilder.build(typeName, generics, isPointer, context)
    }

    fun processGenerics(ctx: LibSLParser.GenericContext): MutableList<Generic> {
        val genericReferences = mutableListOf<Generic>()
        ctx.genericPart().forEach {
            val genericModifier = if(it.genericModifier == null) {
                ""
            } else {
                GenericModifier.fromString(it.genericModifier.text).string
            }
            val generic = getRealType(it.typeIdentifier())
            val genericRef = generic.getReference(context)
            genericReferences.add(
                Generic(
                    genericModifier,
                    genericRef
                )
            )
        }
        return genericReferences
    }

    private fun getRealType(ctx: TypeIdentifierContext): RealType {
        val typeNameParts = ctx.name.asPeriodSeparatedParts()
        val isPointer = ctx.asterisk != null

        val generics = mutableListOf<Generic>()

        if(ctx.generic() != null) {
            generics.addAll(processGenerics(ctx.generic()))
        }

        val realType = RealType(
            typeNameParts,
            isPointer,
            generics,
            context,
            posGetter.getCtxPosition(context.fileName, ctx)
        )

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
        val generics = mutableListOf<Generic>()

        if(ctx.generic() != null) {
            generics.addAll(processGenerics(ctx.generic()))
        }

        return ArrayType(
            isPointer,
            generics,
            context
        )
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
        val args = if(ctx.annotationArgs() != null) {
            processAnnotationArgs(ctx)
        } else {
            emptyList()
        }

        val argTypes = args.map { argument -> context.typeInferrer.getExpressionType(argument.value).getReference(context) }
        val annotationRef = AnnotationReferenceBuilder.build(name, argTypes, context)

        return AnnotationUsage(
            annotationRef,
            args,
            posGetter.getCtxPosition(context.fileName, ctx)
        )
    }

    private fun processAnnotationArgs(ctx: LibSLParser.AnnotationUsageContext): List<NamedArgumentWithValue> {
        val namedArgs = mutableListOf<NamedArgumentWithValue>()
        ctx.annotationArgs().forEach { a ->
            val name = a.argName()?.name?.text
            val value = ExpressionVisitor(context).visitExpression(a.expression())
            namedArgs.add(
                NamedArgumentWithValue(
                    name,
                    value,
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
            )
        }

        return namedArgs
    }
}

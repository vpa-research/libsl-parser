package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLBaseVisitor
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.*

class Resolver(private val context: LslContext) : LibSLBaseVisitor<Unit>() {
    override fun visitFile(ctx: LibSLParser.FileContext) {
        visitTypesSectionBody(ctx.typesSection().typesSectionBody())

        for (automaton in ctx.declarations().declaration().mapNotNull { it.automatonDecl() }) {
            visitAutomatonDecl(automaton)
        }

        for (extensionFunction in ctx.declarations().declaration().mapNotNull { it.functionDecl() }) {
            visitFunctionDecl(extensionFunction)
        }
    }

    override fun visitTypesSectionBody(ctx: LibSLParser.TypesSectionBodyContext) {
        for (semanticTypeContext in ctx.semanticType()) {
            val type = when {
                semanticTypeContext.simpleSemanticType() != null -> {
                    val semanticType = semanticTypeContext.simpleSemanticType().semanticTypeName.text
                    val realTypeCtx = semanticTypeContext.simpleSemanticType().realTypeName()
                    val realName = processRealTypeName(realTypeCtx)
                    SimpleType(semanticType, realName, context)
                }
                semanticTypeContext.enumLikeSemanticType() != null -> {
                    val enum = semanticTypeContext.enumLikeSemanticType()
                    val semanticType = enum.semanticTypeName.text
                    val realName = processRealTypeName(enum.realTypeName())
                    val body = enum.enumLikeSemanticTypeBody().enumLikeSemanticTypeBodyStatement()
                        .map { it.Identifier().text to it.expressionAtomic().text }
                    EnumLikeType(semanticType, realName, body, context)
                }
                else -> error("unknown type's type")
            }

            context.storeResolvedType(type)
        }
    }

    private fun processRealTypeName(ctx: LibSLParser.RealTypeNameContext): RealType {
        val name = ctx.periodSeparatedFullName().first().Identifier().map { it.text }
        val generic = ctx.generic?.Identifier()?.map { it.text }

        return RealType(
            name,
            generic
        )
    }

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val name = ctx.name.text

        val automaton = Automaton(
            name,
            AutomatonKind.REAL,
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf()
        )
        context.storeResolvedAutomaton(automaton)

        for (functionDecl in ctx.automatonStatement().mapNotNull { it.functionDecl() }) {
            visitFunctionDecl(functionDecl)
        }
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val (automatonName, name) = parseFunctionName(ctx)

        val typeName = ctx.functionType?.text
        val returnType = if (typeName != null) context.resolveType(typeName)
            ?: error("unresolved type: $typeName") else null

        val args = ctx.functionDeclArgList()?.parameter()?.map { arg ->
            val argType = context.resolveType(arg.type.text) ?: error("unresolved type")
            Argument(arg.name.text, argType)
        }?.toList().orEmpty()

        val func = SimpleFunction(name, automatonName, args, returnType, listOf(), listOf(), context)
        context.storeResolvedFunction(func)
    }
}
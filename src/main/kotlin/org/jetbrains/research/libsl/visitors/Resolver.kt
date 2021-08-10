package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLBaseVisitor
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.*

class Resolver(private val context: LslContext) : LibSLBaseVisitor<Unit>() {
    private val asgBuilderVisitor = ASGBuilder(context)

    override fun visitFile(ctx: LibSLParser.FileContext) {
        visitTypesSectionBody(ctx.typesSection().typesSectionBody())

        val automata = ctx.declarations().declaration().mapNotNull { it.automatonDecl() }
        for (automatonCtx in automata) {
            val typeName = automatonCtx.type.text
            val type = context.resolveType(typeName) ?: error("unresolved type: $typeName")
            val automaton = Automaton(
                automatonCtx.name.text,
                type,
                listOf(),
                listOf(),
                listOf(),
                listOf(),
                listOf(),
            )
            context.storeResolvedAutomaton(automaton)
        }

        for (automaton in automata) {
            visitAutomatonDecl(automaton)
        }

        for (extensionFunction in ctx.declarations().declaration().mapNotNull { it.functionDecl() }) {
            visitFunctionDecl(extensionFunction)
        }

        ctx.declarations().declaration().mapNotNull { it.variableDeclaration() }.map { variableDecl ->
            val nameWithType = variableDecl.nameWithType()
            val type = context.resolveType(nameWithType.type.text) ?: error("unresolved type: ${nameWithType.type.text}")
            val init = if (variableDecl.assignmentRight() != null){
                asgBuilderVisitor.processAssignmentRight(variableDecl.assignmentRight())
            } else {
                null
            }

            val variable = Variable(
                nameWithType.name.text,
                type,
                init
            )
            context.storeGlobalVariable(variable)
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

        val variables = ctx.automatonStatement()
            .mapNotNull { it.variableDeclaration() }
            .map { decl ->
                val variableName = decl.nameWithType().name.text
                val typeName = decl.nameWithType().type.text
                if (decl.assignmentRight() != null) {
                    val init: Atomic = asgBuilderVisitor.processAssignmentRight(decl.assignmentRight())
                    getVariable(variableName, typeName, init, context)
                } else {
                    getVariable(variableName, typeName, null, context)
                }
            }

        val constructorVariables = ctx.nameWithType().map { getVariable(it.name.text, it.type.text, null, context) }

        val automaton = context.resolveAutomaton(name)?.apply {
            this.internalVariables = variables
            this.constructorVariables = constructorVariables
        } ?: error("")
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

        val func = Function(name, automatonName, args, returnType, listOf(), listOf(), context)
        context.storeResolvedFunction(func)
    }
}
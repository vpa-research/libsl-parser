package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.EnumSemanticTypeEntryContext
import org.jetbrains.research.libsl.LibSLParser.FunctionDeclContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.type.*
import org.jetbrains.research.libsl.utils.Position

class TypeResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    context: LslGlobalContext
) : LibSLParserVisitor<Unit>(context) {
    override fun visitSimpleSemanticType(ctx: LibSLParser.SimpleSemanticTypeContext) {
        val typeName = ctx.semanticName.name.asPeriodSeparatedString()
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())
        val realNameCtx = ctx.realName
        val originType = getRealTypeOrArray(realNameCtx)

        val type = SimpleType(
            typeName,
            originType,
            annotationReferences,
            context = context
        )

        context.storeType(originType)
        context.storeType(type)
    }

    override fun visitEnumSemanticType(ctx: LibSLParser.EnumSemanticTypeContext) {
        val typeName = ctx.semanticName.asPeriodSeparatedString()
        val realTypeCtx = ctx.realName
        val originType = getRealTypeOrArray(realTypeCtx)
        val entriesContexts = ctx.enumSemanticTypeEntry()
        val entries = processBlockTypeStatements(entriesContexts)
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val type = EnumLikeSemanticType(
            typeName,
            originType,
            entries,
            annotationReferences,
            context
        )

        context.storeType(originType)
        context.storeType(type)
    }

    private fun processBlockTypeStatements(statementsContexts: List<EnumSemanticTypeEntryContext>): Map<String, Atomic> {
        return statementsContexts.map { ctx -> processBlockTypeStatement(ctx) }.associate { it }
    }

    private fun processBlockTypeStatement(statementContext: EnumSemanticTypeEntryContext): Pair<String, Atomic> {
        val entryName = statementContext.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val atomicValueContext = statementContext.expressionAtomic()
        val atomicValue = expressionVisitor.visitExpressionAtomic(atomicValueContext)

        return entryName to atomicValue
    }

    override fun visitTypealiasStatement(ctx: LibSLParser.TypealiasStatementContext) {
        val name = ctx.left.periodSeparatedFullName().asPeriodSeparatedString()
        val originalTypeReference = processTypeIdentifier(ctx.right)
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val type = TypeAlias(name, originalTypeReference, annotationReferences, context)

        context.storeType(type)
    }

    override fun visitEnumBlock(ctx: LibSLParser.EnumBlockContext) {
        val name = ctx.typeIdentifier().text.extractIdentifier()
        val statementsContexts = ctx.enumBlockStatement()
        val statements = processEnumStatements(statementsContexts)
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val type = EnumType(name, statements, annotationReferences, context)

        context.storeType(type)
    }

    private fun processEnumStatements(statements: List<LibSLParser.EnumBlockStatementContext>): Map<String, Atomic> {
        return statements.map { processEnumStatement(it) }.associate { it }
    }

    private fun processEnumStatement(statement: LibSLParser.EnumBlockStatementContext): Pair<String, Atomic> {
        val name = statement.Identifier().asPeriodSeparatedString()

        val expressionVisitor = ExpressionVisitor(context)
        val atomicContext = statement.integerNumber()
        val atomic = expressionVisitor.visit(atomicContext) as Atomic

        return name to atomic
    }

    override fun visitTypeDefBlock(ctx: LibSLParser.TypeDefBlockContext) {
        val name = ctx.name.asPeriodSeparatedString()
        val isTypeIdentifier = ctx.targetType()?.typeIdentifier()?.name?.text
        val forTypeList = mutableListOf<String>()
        ctx.targetType()?.typeList()?.typeIdentifier()?.forEach { forTypeList.add(it.name.text)}

        val variables = mutableListOf<Variable>()
        val functions = mutableListOf<org.jetbrains.research.libsl.nodes.Function>()
        ctx.typeDefBlockStatement().forEach { statement ->
            when {
                statement.variableDecl() != null ->
                    variables.add(processVariableDecl(statement.variableDecl()))

                statement.functionDecl() != null ->
                    functions.add(processFunctionDecl(statement.functionDecl()))
            }
        }

        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())
        val typeGenericDeclBlock = mutableListOf<TypeGenericDecl>()
        if(ctx.typeDefGenericDeclBlock() != null) {
            ctx.typeDefGenericDeclBlock().nameWithType().forEach {
                typeGenericDeclBlock.add(TypeGenericDecl(it.name.text, processTypeIdentifier(it.type),
                    Position(context.fileName, ctx.position().first, ctx.position().second))
                )
            }
        }

        val type = StructuredType(name, variables, functions, isTypeIdentifier, forTypeList, typeGenericDeclBlock, annotationReferences, context)
        if(ctx.generic() != null) {
            type.generics.addAll(processGenerics(ctx.generic().typeIdentifier()))
        }
        context.storeType(type)
    }

    private fun processVariableDecl(ctx: LibSLParser.VariableDeclContext): Variable {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val name = ctx.nameWithType().name.asPeriodSeparatedString()
        val typeReference = processTypeIdentifier(ctx.nameWithType().type)
        val expressionVisitor = ExpressionVisitor(context)
        val initValue = ctx.expression()?.let { right -> expressionVisitor.visitExpression(right) }

        val variable = VariableWithInitialValue(
            keyword,
            name,
            typeReference,
            getAnnotationUsages(ctx.annotationUsage()),
            initValue,
            Position(context.fileName, ctx.position().first, ctx.position().second)
        )

        context.storeVariable(variable)
        return variable
    }

    private fun processFunctionDecl(ctx: FunctionDeclContext): org.jetbrains.research.libsl.nodes.Function {
        val functionContext = FunctionContext(context)
        val isStatic = ctx.STATIC() != null
        val functionName = ctx.functionName.text.extractIdentifier()

        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        val returnType = ctx.functionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(returnType,
                Position(context.fileName, ctx.position().first, ctx.position().second)
            )
            context.storeVariable(resultVariable)
        }

        return Function(
            kind = FunctionKind.FUNCTION,
            functionName,
            automatonReference = null,
            args,
            returnType,
            annotationReferences,
            hasBody = false,
            targetAutomatonRef = null,
            context = functionContext,
            isStatic = isStatic,
            position = Position(context.fileName, ctx.position().first, ctx.position().second)
        )
    }

    private val FunctionDeclContext.args: List<FunctionArgument>
        get() = this
            .functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->
                val typeRef = processTypeIdentifier(parameter.type)
                val annotationsReferences = getAnnotationUsages(parameter.annotationUsage())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i,
                    annotationsReferences,
                    targetAutomaton = null,
                    Position(context.fileName, parameter.position().first, parameter.position().second)
                )
                arg
            }
            .orEmpty()
}

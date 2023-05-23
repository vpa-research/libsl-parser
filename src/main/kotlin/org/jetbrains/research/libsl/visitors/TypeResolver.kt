package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.tree.TerminalNode
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.EnumSemanticTypeEntryContext
import org.jetbrains.research.libsl.LibSLParser.TypeDefBlockStatementContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.Atomic
import org.jetbrains.research.libsl.nodes.Variable
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.*

class TypeResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    context: LslGlobalContext
) : LibSLParserVisitor<Unit>(context) {
    override fun visitSimpleSemanticType(ctx: LibSLParser.SimpleSemanticTypeContext) {
        val typeName = ctx.semanticName.name.asPeriodSeparatedString()

        val realNameCtx = ctx.realName
        val originType = getRealTypeOrArray(realNameCtx)

        val type = SimpleType(
            typeName,
            originType,
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

        val type = EnumLikeSemanticType(
            typeName,
            originType,
            entries,
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

        val type = TypeAlias(name, originalTypeReference, context)

        context.storeType(type)
    }

    override fun visitEnumBlock(ctx: LibSLParser.EnumBlockContext) {
        val name = ctx.typeIdentifier().text.extractIdentifier()
        val statementsContexts = ctx.enumBlockStatement()
        val statements = processEnumStatements(statementsContexts)

        val type = EnumType(name, statements, context)

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

        val statements = mutableListOf<TypeDefBlockStatement>()
        ctx.typeDefBlockStatement().forEach { statements.add(processTypeDefBlockStatement(it)) }

        val type = StructuredType(name, statements, isTypeIdentifier, forTypeList, context)
        context.storeType(type)
    }

    private fun processTypeDefBlockStatement(ctx: TypeDefBlockStatementContext): TypeDefBlockStatement {
        val name = ctx.nameWithType().name.text.extractIdentifier()
        val fields = mutableListOf<Variable>()
        ctx.nameWithType().nameWithType().forEach { fields.add(Variable(it.name.text, processTypeIdentifier(it.type))) }
        val typeRef = processTypeIdentifier(ctx.nameWithType().type)

        return TypeDefBlockStatement(name, fields, typeRef)
    }
}

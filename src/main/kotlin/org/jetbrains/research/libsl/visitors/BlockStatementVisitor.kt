package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.utils.Position

class BlockStatementVisitor(
    private val functionContext: FunctionContext,
    private val statements: MutableList<Statement>
    ) : LibSLParserVisitor<Unit>(functionContext) {

    override fun visitExpression(ctx: LibSLParser.ExpressionContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val expression = ExpressionStatement(expressionVisitor.visitExpression(ctx), Position(context.fileName, ctx.position()))
        statements.add(expression)
    }

    override fun visitVariableAssignment(ctx: LibSLParser.VariableAssignmentContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
        val op = AssignOps.fromString(ctx.op.text)
        val value = expressionVisitor.visitExpression(ctx.expression())
        val assignment = Assignment(left, op, value, Position(context.fileName, ctx.position()))
        statements.add(assignment)
    }

    override fun visitIfStatement(ifCtx: LibSLParser.IfStatementContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val value = expressionVisitor.visitExpression(ifCtx.expression())

        val ifStatements = mutableListOf<Statement>()
        val ifStatementVisitor = BlockStatementVisitor(functionContext, ifStatements)
        ifCtx.functionBodyStatements().forEach { ifStatementVisitor.visit(it) }

        val elseStatement = ifCtx.elseStatement()?.let { elseStmt ->
            val elseStatements = mutableListOf<Statement>()
            val elseStatementsVisitor = BlockStatementVisitor(functionContext, elseStatements)
            elseStmt.functionBodyStatements().forEach { elseStatementsVisitor.visit(it) }

            ElseStatement(elseStatements, Position(context.fileName, ifCtx.position()))
        }

        val ifBlock = IfStatement(value, ifStatements, elseStatement, Position(context.fileName, ifCtx.position()))

        statements.add(ifBlock)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
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
            Position(context.fileName, ctx.position())
        )
        val variableDeclaration = VariableDeclaration(variable, Position(context.fileName, ctx.position()))
        statements.add(variableDeclaration)
        context.storeVariable(variable)
    }

    override fun visitElseStatement(ctx: LibSLParser.ElseStatementContext) {
        error("Unreachable")
    }
}

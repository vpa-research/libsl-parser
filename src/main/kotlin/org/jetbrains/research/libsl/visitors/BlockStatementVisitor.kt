package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.*

class BlockStatementVisitor(
    private val functionContext: FunctionContext,
    private val statements: MutableList<Statement>
) : LibSLParserVisitor<Unit>(functionContext) {

    override fun visitVariableAssignment(ctx: LibSLParser.VariableAssignmentContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
        val value = expressionVisitor.visitAssignmentRight(ctx.assignmentRight())
        val assignment = Assignment(left, value)

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

            ElseStatement(elseStatements)
        }

        val ifBlock = IfStatement(value, ifStatements, elseStatement)

        statements.add(ifBlock)
    }

    override fun visitAction(ctx: LibSLParser.ActionContext) {
        val name = ctx.Identifier().text.extractIdentifier()
        val expressionVisitor = ExpressionVisitor(functionContext)
        val args = ctx.expressionsList().expression().map { expr ->
            expressionVisitor.visitExpression(expr)
        }.toMutableList()

        val action = Action(name, args)

        statements.add(action)
    }

    override fun visitElseStatement(ctx: LibSLParser.ElseStatementContext) {
        error("Unreachable")
    }
}

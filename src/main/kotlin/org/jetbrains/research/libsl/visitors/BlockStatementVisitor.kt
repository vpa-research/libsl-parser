package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.*

class BlockStatementVisitor(
    private val functionContext: FunctionContext
) : LibSLParserVisitor<Unit>(functionContext) {
    val statements: MutableList<Statement> = mutableListOf()

    override fun visitExpression(ctx: LibSLParser.ExpressionContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val expression = ExpressionStatement(expressionVisitor.visitExpression(ctx))
        statements.add(expression)
    }

    override fun visitVariableAssignment(ctx: LibSLParser.VariableAssignmentContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
        val op = AssignOps.fromString(ctx.op.text)
        val value = ctx.assignmentRight().let { expressionVisitor.visitAssignmentRight(it) }
        val assignment = Assignment(left, op, value)
        statements.add(assignment)
    }

    override fun visitIfStatement(ifCtx: LibSLParser.IfStatementContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val value = expressionVisitor.visitExpression(ifCtx.expression())

        val ifStatementVisitor = BlockStatementVisitor(functionContext)
        ifCtx.functionBodyStatement().forEach { ifStatementVisitor.visit(it) }
        val ifStatements = ifStatementVisitor.statements

        val elseStatement = ifCtx.elseStatement()?.let { elseStmt ->

            val elseStatementsVisitor = BlockStatementVisitor(functionContext)
            elseStmt.functionBodyStatement().forEach { elseStatementsVisitor.visit(it) }
            val elseStatements = elseStatementsVisitor.statements
            ElseStatement(elseStatements)
        }

        val ifBlock = IfStatement(
            value,
            ifStatements,
            elseStatement
        )

        statements.add(ifBlock)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val name = ctx.nameWithType().name.asPeriodSeparatedString()
        val typeReference = processTypeIdentifier(ctx.nameWithType().type)
        val expressionVisitor = ExpressionVisitor(context)
        val initValue = ctx.assignmentRight()?.let { expressionVisitor.visitAssignmentRight(it) }

            val variable = VariableWithInitialValue(
            keyword,
            name,
            typeReference,
            getAnnotationUsages(ctx.annotationUsage()),
            initValue
        )
        val variableDeclaration = VariableDeclaration(variable)
        statements.add(variableDeclaration)
        context.storeVariable(variable)
    }

    override fun visitElseStatement(ctx: LibSLParser.ElseStatementContext) {
        error("Unreachable")
    }
}

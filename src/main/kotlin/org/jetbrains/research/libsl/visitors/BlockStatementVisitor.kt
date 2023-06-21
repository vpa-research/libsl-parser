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
        val value = if (ctx.assignmentRight() != null) {
            expressionVisitor.visitAssignmentRight(ctx.assignmentRight())
        } else {
            expressionVisitor.visitExpression(ctx.expression())
        }
        val assignment = Assignment(left, op, value)
        statements.add(assignment)
    }

    override fun visitIfStatement(ifCtx: LibSLParser.IfStatementContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val value = expressionVisitor.visitExpression(ifCtx.expression())

        val ifStatementVisitor = BlockStatementVisitor(functionContext)
        ifCtx.functionBodyStatements().forEach { ifStatementVisitor.visit(it) }
        val ifStatements = ifStatementVisitor.statements

        val elseStatement = ifCtx.elseStatement()?.let { elseStmt ->
            val elseStatementsVisitor = BlockStatementVisitor(functionContext)
            elseStmt.functionBodyStatements().forEach { elseStatementsVisitor.visit(it) }
            val elseStatements = elseStatementsVisitor.statements
            ElseStatement(elseStatements)
        }

        val ifBlock = IfStatement(value, ifStatements, elseStatement)

        statements.add(ifBlock)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val name = ctx.nameWithType().name.asPeriodSeparatedString()
        val typeReference = processTypeIdentifier(ctx.nameWithType().type)
        val expressionVisitor = ExpressionVisitor(context)
        val initValue = ctx.assignmentRight()?.let { right ->
            when {
                right.callAutomatonConstructorWithNamedArgs() != null -> {
                    expressionVisitor.visitCallAutomatonConstructorWithNamedArgs(right.callAutomatonConstructorWithNamedArgs())
                }
                right.expression() != null -> {
                    expressionVisitor.visitExpression(right.expression())
                }
                else -> error("unknown initializer kind")
            }
        }

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

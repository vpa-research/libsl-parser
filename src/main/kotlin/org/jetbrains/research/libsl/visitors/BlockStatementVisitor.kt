package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.*

class BlockStatementVisitor(
    private val functionContext: FunctionContext,
    private val statements: MutableList<Statement>,
    private val localVariables: MutableList<Variable>
) : LibSLParserVisitor<Unit>(functionContext) {

    override fun visitVariableAssignment(ctx: LibSLParser.VariableAssignmentContext) {
        when {
            ctx.assignmentRight() != null -> let{
                val expressionVisitor = ExpressionVisitor(functionContext)
                val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
                val value = expressionVisitor.visitAssignmentRight(ctx.assignmentRight())
                val assignment = Assignment(left, value)
                statements.add(assignment)
            }
            ctx.expression() != null -> let{
                val expressionVisitor = ExpressionVisitor(functionContext)
                val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
                val op = CompoundOps.fromString(ctx.compoundAssignOp.text)
                val value = expressionVisitor.visitExpression(ctx.expression())
                val assignmentWithCompoundOp = AssignmentWithCompoundOp(left, op, value)
                statements.add(assignmentWithCompoundOp)
            }
            ctx.leftUnaryOp != null -> let {
                val expressionVisitor = ExpressionVisitor(functionContext)
                val op = ArithmeticUnaryOp.fromString(ctx.leftUnaryOp.text)
                val value = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
                val assignmentWithLeftUnaryOp = AssignmentWithLeftUnaryOp(op, value)
                statements.add(assignmentWithLeftUnaryOp)
            }
            ctx.rightUnaryOp != null -> let {
                val expressionVisitor = ExpressionVisitor(functionContext)
                val op = ArithmeticUnaryOp.fromString(ctx.rightUnaryOp.text)
                val value = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
                val assignmentWithRightUnaryOp = AssignmentWithRightUnaryOp(op, value)
                statements.add(assignmentWithRightUnaryOp)
            }
        }
    }

    override fun visitIfStatement(ifCtx: LibSLParser.IfStatementContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val value = expressionVisitor.visitExpression(ifCtx.expression())

        val ifStatements = mutableListOf<Statement>()
        val ifStatementVisitor = BlockStatementVisitor(functionContext, ifStatements, localVariables)
        ifCtx.functionBodyStatements().forEach { ifStatementVisitor.visit(it) }

        val elseStatement = ifCtx.elseStatement()?.let { elseStmt ->
            val elseStatements = mutableListOf<Statement>()
            val elseStatementsVisitor = BlockStatementVisitor(functionContext, elseStatements, localVariables)
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

    override fun visitProc(ctx: LibSLParser.ProcContext) {
        val name = ctx.Identifier().text.extractIdentifier()
        val expressionVisitor = ExpressionVisitor(functionContext)
        val args = ctx.expressionsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }?.toMutableList()
        val hasThisExpression = ctx.THIS() != null

        val procedureCall = ProcedureCall(name, args, hasThisExpression)

        statements.add(procedureCall)
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
        localVariables.add(variable)
        statements.add(variableDeclaration)
        context.storeVariable(variable)
    }

    override fun visitElseStatement(ctx: LibSLParser.ElseStatementContext) {
        error("Unreachable")
    }
}

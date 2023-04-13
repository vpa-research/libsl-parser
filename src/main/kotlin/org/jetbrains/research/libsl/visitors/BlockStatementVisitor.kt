package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.AnnotationReference

class BlockStatementVisitor(
    private val functionContext: FunctionContext,
    private val statements: MutableList<Statement>,
    private val localVariables: MutableList<Variable>
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


    // TODO(Expressions can not be printed out)
    override fun visitExpression(ctx: LibSLParser.ExpressionContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        ctx.expression().forEach { expr ->
            println(expressionVisitor.visitExpression(expr))
        }
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKeyword.fromString(ctx.keyword.text)
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

        val variable = VariableWithInitialValue(keyword, name, typeReference, getVariableAnnotationList(ctx.variableAnnotations()),  initValue)
        localVariables.add(variable)
        context.storeVariable(variable)
    }

    private fun getVariableAnnotationList(ctx: List<LibSLParser.VariableAnnotationsContext>): MutableList<AnnotationReference> {
        val annotationReferenceList = mutableListOf<AnnotationReference>()
        val annotationReferences = ctx.mapNotNull { processVariableAnnotation(it) }
        annotationReferenceList.addAll(annotationReferences)
        return annotationReferenceList
    }

    private fun processVariableAnnotation(ctx: LibSLParser.VariableAnnotationsContext?): AnnotationReference? {
        ctx ?: return null
        val name = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val args = ctx.expressionsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }.orEmpty().toMutableList()

        context.storeAnnotation(Annotation(name, args))

        return AnnotationReference(name, context)
    }

    override fun visitElseStatement(ctx: LibSLParser.ElseStatementContext) {
        error("Unreachable")
    }
}

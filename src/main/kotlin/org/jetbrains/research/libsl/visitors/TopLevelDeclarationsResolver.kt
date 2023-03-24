package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder

class TopLevelDeclarationsResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val globalContext: LslGlobalContext
) : LibSLParserVisitor<Unit>(globalContext)  {

    override fun visitAnnotationDecl(ctx: LibSLParser.AnnotationDeclContext) {

        val annotationName = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val params = mutableListOf<DeclaredAnnotationParams>()

        ctx.annotationDeclParams()?.annotationDeclParamsPart()?.forEach { parameterCtx ->
            val param = DeclaredAnnotationParams(parameterCtx.nameWithType().name.text.extractIdentifier(),
            processTypeIdentifier(parameterCtx.nameWithType().type),
            parameterCtx.assignmentRight()?.let {
                expressionVisitor.visitAssignmentRight(it)
            })
            params.add(param)
        }

        val declaredAnnotation = DeclaredAnnotation(annotationName, params)
        globalContext.storeDeclaredAnnotation(declaredAnnotation)
    }
    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val automatonContext = AutomatonContext(context)
        AutomatonResolver(basePath, errorManager, automatonContext).visitAutomatonDecl(ctx)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val parentContext = if (ctx.automatonName != null) {
            val automatonRef = AutomatonReferenceBuilder.build(ctx.automatonName.text.extractIdentifier(), context)
            globalContext.resolveAutomaton(automatonRef)!!.context
        } else {
            globalContext
        }

        val functionContext = FunctionContext(parentContext)
        FunctionVisitor(functionContext, parentAutomaton = null, errorManager).visitFunctionDecl(ctx)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val variableName = ctx.nameWithType().name.text.extractIdentifier()
        val typeRef = processTypeIdentifier(ctx.nameWithType().type)

        val expressionVisitor = ExpressionVisitor(context)
        val initialValue = ctx.assignmentRight()?.let { expressionVisitor.visitAssignmentRight(it) }

        val variable = VariableWithInitialValue(variableName, typeRef, initialValue)
        globalContext.storeVariable(variable)
    }

    override fun visitActionDecl(ctx: LibSLParser.ActionDeclContext) {

        val actionName = ctx.actionName.text.extractIdentifier()
        val actionParams = mutableListOf<DeclaredActionParameter>()

        ctx.actionDeclParamList().actionParameter().forEach { param ->
            val actionParam = DeclaredActionParameter(param.name.text.extractIdentifier(),
                processTypeIdentifier(param.type))
            actionParams.add(actionParam) }

        val returnType = ctx.actionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(returnType)
            context.storeVariable(resultVariable)
        }

        val declaredAction = ActionDecl(actionName, actionParams, returnType)

        globalContext.storeDeclaredAction(declaredAction)
    }
}

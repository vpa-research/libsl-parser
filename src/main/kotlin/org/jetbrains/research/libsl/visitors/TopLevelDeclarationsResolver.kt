package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder

class TopLevelDeclarationsResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val globalContext: LslGlobalContext
) : LibSLParserVisitor<Unit>(globalContext)  {

    override fun visitAnnotationDecl(ctx: LibSLParser.AnnotationDeclContext) {
        val annotationName = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val params = mutableListOf<AnnotationParams>()

        ctx.annotationDeclParams()?.annotationDeclParamsPart()?.forEach { parameterCtx ->
            val param = AnnotationParams(parameterCtx.nameWithType().name.text.extractIdentifier(),
            processTypeIdentifier(parameterCtx.nameWithType().type),
            parameterCtx.assignmentRight()?.let {
                expressionVisitor.visitAssignmentRight(it)
            })
            params.add(param)
        }

        val declaredAnnotation = Annotation(annotationName, params)
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
}

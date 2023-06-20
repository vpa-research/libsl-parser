package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.type.VoidType

class TopLevelDeclarationsResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val globalContext: LslGlobalContext
) : LibSLParserVisitor<Unit>(globalContext) {

    override fun visitAnnotationDecl(ctx: LibSLParser.AnnotationDeclContext) {
        val annotationName = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(context)
        val params = mutableListOf<AnnotationArgumentDescriptor>()

        ctx.annotationDeclParams()?.annotationDeclParamsPart()?.map { parameterCtx ->
            val param = AnnotationArgumentDescriptor(
                parameterCtx.nameWithType().name.text.extractIdentifier(),
                processTypeIdentifier(parameterCtx.nameWithType().type),
                parameterCtx.expression()?.let {
                    expressionVisitor.visitExpression(it)
                }
            )
            params.add(param)
        }

        val annotation = Annotation(annotationName, params)
        globalContext.storeAnnotation(annotation)
    }

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val automatonContext = AutomatonContext(context)
        AutomatonResolver(basePath, errorManager, globalContext, automatonContext).visitAutomatonDecl(ctx)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val parentContext = if (ctx.automatonName != null) {
            val automatonRef = AutomatonReferenceBuilder.build(ctx.automatonName.text.extractIdentifier(), context)
            globalContext.resolveAutomaton(automatonRef)!!.context
        } else {
            globalContext
        }

        val functionContext = FunctionContext(parentContext)
        FunctionVisitor(functionContext, parentAutomaton = null, globalContext, errorManager).visitFunctionDecl(ctx)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val variableName = ctx.nameWithType().name.text.extractIdentifier()
        val typeRef = processTypeIdentifier(ctx.nameWithType().type)

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

        val annotationUsages = getAnnotationUsages(ctx.annotationUsage())
        val variable = VariableWithInitialValue(
            keyword,
            variableName,
            typeRef,
            annotationUsages,
            initValue
        )
        globalContext.storeVariable(variable)
    }

    override fun visitActionDecl(ctx: LibSLParser.ActionDeclContext) {
        val actionName = ctx.actionName.text.extractIdentifier()
        val actionParams = mutableListOf<ActionArgumentDescriptor>()

        ctx.actionDeclParamList()?.actionParameter()?.map { parameterCtx ->
            val param = ActionArgumentDescriptor(
                parameterCtx.name.text.extractIdentifier(),
                processTypeIdentifier(parameterCtx.type)
            )
            actionParams.add(param)
        }

        val returnType = ctx.actionType?.let { processTypeIdentifier(it) }

        val actionAnnotations = getAnnotationUsages(ctx.annotationUsage())

        val declaredAction =
            Action(actionName, actionParams, actionAnnotations, returnType)

        globalContext.storeDeclaredAction(declaredAction)
    }
}

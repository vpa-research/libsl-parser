package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.utils.Position

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

        val annotation = Annotation(annotationName, params, Position(context.fileName, ctx.position().first, ctx.position().second))
        globalContext.storeAnnotation(annotation)
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

    override fun visitTypeDefBlock(ctx: LibSLParser.TypeDefBlockContext) {
        //TODO ()
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val variableName = ctx.nameWithType().name.text.extractIdentifier()
        val typeRef = processTypeIdentifier(ctx.nameWithType().type)

        val expressionVisitor = ExpressionVisitor(context)
        val initialValue = ctx.expression()?.let { expressionVisitor.visitExpression(it) }

        val annotationUsages = getAnnotationUsages(ctx.annotationUsage())
        val variable = VariableWithInitialValue(
            keyword,
            variableName,
            typeRef,
            annotationUsages,
            initialValue,
            Position(context.fileName, ctx.position().first, ctx.position().second)
        )

        globalContext.storeVariable(variable)
    }

    override fun visitActionDecl(ctx: LibSLParser.ActionDeclContext) {
        val actionName = ctx.actionName.text.extractIdentifier()
        val actionParams = mutableListOf<DeclaredActionParameter>()

        ctx.actionDeclParamList()?.actionParameter()?.map { param ->
            val actionParam = DeclaredActionParameter(
                param.name.text.extractIdentifier(),
                processTypeIdentifier(param.type),
                getAnnotationUsages(param.annotationUsage()),
                Position(context.fileName, ctx.position().first, ctx.position().second)
            )

            actionParams.add(actionParam)
        }

        val returnType = ctx.actionType?.let { processTypeIdentifier(it) }
        val actionAnnotations = getAnnotationUsages(ctx.annotationUsage())
        val declaredAction = ActionDecl(actionName, actionParams, actionAnnotations, returnType,
            Position(context.fileName, ctx.position().first, ctx.position().second)
        )
        globalContext.storeDeclaredAction(declaredAction)
    }
}

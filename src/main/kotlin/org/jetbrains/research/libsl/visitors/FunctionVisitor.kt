package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.FunctionParamAnnotationContext
import org.jetbrains.research.libsl.LibSLParser.ExpressionContext
import org.jetbrains.research.libsl.LibSLParser.FunctionDeclContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.UnspecifiedAutomaton
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder.getReference

class FunctionVisitor(
    private val functionContext: FunctionContext,
    private var parentAutomaton: Automaton?,
    val errorManager: ErrorManager
) : LibSLParserVisitor<Unit>(functionContext) {
    private lateinit var buildingFunction: Function

    override fun visitFunctionDecl(ctx: FunctionDeclContext) {
        val automatonName = ctx.automatonName?.text?.extractIdentifier()
        if (automatonName == null && parentAutomaton == null) {
            errorManager(UnspecifiedAutomaton("automaton must be specified for top-level functions", ctx.position()))
            return
        }

        check((automatonName != null) xor (parentAutomaton != null))


        val automatonReference = automatonName?.let { AutomatonReferenceBuilder.build(it, functionContext) } ?: parentAutomaton?.getReference(functionContext)
        check(automatonReference != null)

        if (automatonName != null) {
            parentAutomaton = automatonReference.resolveOrError()
        }

        val functionName = ctx.functionName.text.extractIdentifier()

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        val targetAutomatonRef = args.getFunctionTargetByAnnotation ?: automatonReference

        val returnType = ctx.functionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(returnType)
            context.storeVariable(resultVariable)
        }

        buildingFunction = Function(
            functionName,
            automatonReference,
            args,
            returnType,
            hasBody = ctx.functionBody() != null,
            targetAutomatonRef = targetAutomatonRef,
            context = functionContext
        )

        super.visitFunctionDecl(ctx)
        parentAutomaton?.localFunctions?.add(buildingFunction)
    }

    private val FunctionDeclContext.args: List<FunctionArgument>
        get() = this
            .functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->
                val typeRef = processTypeIdentifier(parameter.type)
                val annotation = processAnnotation(parameter.functionParamAnnotation())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i, annotation)
                if (annotation != null && annotation.name == "target") {
                    val targetAutomatonName = typeRef.name
                    val targetAutomatonReference = AutomatonReferenceBuilder.build(targetAutomatonName, context)
                    arg.targetAutomaton = targetAutomatonReference
                    arg.typeReference = targetAutomatonReference.resolveOrError().typeReference
                }

                arg
            }
            .orEmpty()

    private fun processAnnotation(ctx: FunctionParamAnnotationContext?): Annotation? {
        ctx ?: return null

        val name = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(functionContext)
        val args = ctx.argsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }.orEmpty().toMutableList()

        if (name == "target") {
            return TargetAnnotation()
        }

        return Annotation(name, args)
    }

    private val List<FunctionArgument>.getFunctionTargetByAnnotation: AutomatonReference?
        get() {
            val targetArg = firstOrNull { arg -> arg.annotation?.name == "target" } ?: return null
            val automatonName = targetArg.typeReference.name
            return AutomatonReferenceBuilder.build(automatonName, functionContext)
        }

    override fun visitEnsuresContract(ctx: LibSLParser.EnsuresContractContext) {
        processContract(ctx.name?.text?.extractIdentifier(), ContractKind.ENSURES, ctx.expression())
    }

    override fun visitRequiresContract(ctx: LibSLParser.RequiresContractContext) {
        processContract(ctx.name?.text?.extractIdentifier(), ContractKind.REQUIRES, ctx.expression())
    }

    override fun visitAssignsContract(ctx: LibSLParser.AssignsContractContext) {
        processContract(ctx.name?.text?.extractIdentifier(), ContractKind.ASSIGNS, ctx.expression())
    }

    private fun processContract(name: String?, kind: ContractKind, expressionContext: ExpressionContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val expression = expressionVisitor.visitExpression(expressionContext)

        val contract = Contract(name, expression, kind)
        buildingFunction.contracts.add(contract)
    }

    override fun visitVariableAssignment(ctx: LibSLParser.VariableAssignmentContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
        val value = expressionVisitor.visitAssignmentRight(ctx.assignmentRight())
        val assignment = Assignment(left, value)

        buildingFunction.statements.add(assignment)
    }

    override fun visitAction(ctx: LibSLParser.ActionContext) {
        val name = ctx.Identifier().text.extractIdentifier()
        val expressionVisitor = ExpressionVisitor(functionContext)
        val args = ctx.argsList().expression().map { expr ->
            expressionVisitor.visitExpression(expr)
        }.toMutableList()

        val action = Action(name, args)
        buildingFunction.statements.add(action)
    }
}

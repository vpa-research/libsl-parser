package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser.*
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder.getReference
import org.jetbrains.research.libsl.utils.PositionGetter
import kotlin.IllegalStateException

class FunctionVisitor(
    private val functionContext: FunctionContext,
    private var parentAutomaton: Automaton?,
    private val globalContext: LslGlobalContext,
    val errorManager: ErrorManager
) : LibSLParserVisitor<Unit>(functionContext) {
    private lateinit var buildingFunction: Function
    private val fileName = context.fileName
    private val posGetter = PositionGetter()

    override fun visitFunctionDecl(ctx: FunctionDeclContext) {
        val automatonName = ctx.functionHeader().automatonName?.text?.extractIdentifier()
        val isMethod = ctx.functionHeader().headerWithAsterisk() != null

        /* if (automatonName == null && parentAutomaton == null) {
            errorManager(UnspecifiedAutomaton("automaton must be specified for top-level functions", ctx.position()))
            return
        } */
        // check((automatonName != null) xor (parentAutomaton != null))

        val automatonReference = automatonName?.let { AutomatonReferenceBuilder.build(it, functionContext) } ?: parentAutomaton?.getReference(functionContext)
        // check(automatonReference != null)

        if (automatonName != null) {
            parentAutomaton = automatonReference?.resolveOrError()
        }

        var isStatic = false
        if(ctx.functionHeader().modifier != null) {
            if(ctx.functionHeader().modifier.text == "static") {
                isStatic = true
            } else {
                throw IllegalStateException("Unknown modifier, only static allowed")
            }
        }

        val functionName = ctx.functionHeader().functionName.text.extractIdentifier()

        val annotationReferences = getAnnotationUsages(ctx.functionHeader().annotationUsage())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        val targetAutomatonRef = args.getFunctionTargetByAnnotation ?: automatonReference
        val returnType = ctx.functionHeader().functionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(
                returnType,
                posGetter.getCtxPosition(fileName, ctx)
            )
            context.storeVariable(resultVariable)
        }

        buildingFunction = Function(
            kind = FunctionKind.FUNCTION,
            functionName,
            automatonReference,
            args,
            returnType,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            targetAutomatonRef = targetAutomatonRef,
            context = functionContext,
            isMethod = isMethod,
            isStatic = isStatic,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )

        super.visitFunctionDecl(ctx)
        parentAutomaton?.localFunctions?.add(buildingFunction)
    }

    override fun visitConstructorDecl(ctx: ConstructorDeclContext) {
        val isMethod = ctx.constructorHeader().headerWithAsterisk() != null
        val constructorName = ctx.constructorHeader().functionName.text.extractIdentifier()
        val annotationReferences = getAnnotationUsages(ctx.constructorHeader().annotationUsage())
        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        buildingFunction = Constructor(
            constructorName,
            args,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            context = functionContext,
            isMethod = isMethod,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )

        super.visitConstructorDecl(ctx)
        parentAutomaton?.constructors?.add(buildingFunction)
    }

    override fun visitDestructorDecl(ctx: DestructorDeclContext) {
        val isMethod = ctx.destructorHeader().headerWithAsterisk() != null
        val destructorName = ctx.destructorHeader().functionName.text.extractIdentifier()
        val annotationReferences = getAnnotationUsages(ctx.destructorHeader().annotationUsage())
        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        buildingFunction = Destructor(
            destructorName,
            args,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            context = functionContext,
            isMethod = isMethod,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )

        super.visitDestructorDecl(ctx)
        parentAutomaton?.destructors?.add(buildingFunction)
    }

    override fun visitProcDecl(ctx: ProcDeclContext) {
        val isMethod = ctx.procHeader().headerWithAsterisk() != null
        val procName = ctx.procHeader().functionName.text.extractIdentifier()
        val annotationReferences = getAnnotationUsages(ctx.procHeader().annotationUsage())
        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }
        val returnType = ctx.procHeader().functionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(
                returnType,
                entityPosition = posGetter.getCtxPosition(fileName, ctx)
            )
            context.storeVariable(resultVariable)
        }

        buildingFunction = Procedure(
            procName,
            args,
            returnType,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            context = functionContext,
            isMethod = isMethod,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )

        super.visitProcDecl(ctx)
        globalContext.storeFunction(buildingFunction)
        parentAutomaton?.procDeclarations?.add(buildingFunction)
    }

    override fun visitFunctionBodyStatement(ctx: FunctionBodyStatementContext) {
        if(parentAutomaton is AutomatonConcept) {
            error("Function realisation inside automaton concept")
        } else {
            val visitor = BlockStatementVisitor(functionContext)
            visitor.visit(ctx)
            val statements = visitor.statements
            buildingFunction.statements.addAll(statements)
        }
    }

    private fun getDeclArgs(functionDeclArgList: FunctionDeclArgListContext?): List<FunctionArgument> {
        return functionDeclArgList?.parameter()?.mapIndexed { i, parameter ->
            val typeRef = processTypeIdentifier(parameter.type)
            val annotationsReferences = getAnnotationUsages(parameter.annotationUsage())
            val arg = FunctionArgument(
                parameter.name.text.extractIdentifier(),
                typeRef,
                i,
                annotationsReferences,
                null,
                posGetter.getCtxPosition(fileName, parameter)
            )

            if (annotationsReferences.any { it.annotationReference.name == "target" }) {
                val targetAutomatonName = typeRef.name
                val targetAutomatonReference = AutomatonReferenceBuilder.build(targetAutomatonName, context)
                arg.targetAutomaton = targetAutomatonReference
                arg.typeReference = targetAutomatonReference.resolveOrError().typeReference
            }

            arg
        }.orEmpty()
    }

    private val FunctionDeclContext.args: List<FunctionArgument>
        get() = getDeclArgs(this.functionHeader().functionDeclArgList())

    private val ConstructorDeclContext.args: List<FunctionArgument>
        get() = getDeclArgs(this.constructorHeader().functionDeclArgList())

    private val DestructorDeclContext.args: List<FunctionArgument>
        get() = getDeclArgs(this.destructorHeader().functionDeclArgList())

    private val ProcDeclContext.args: List<FunctionArgument>
        get() = getDeclArgs(this.procHeader().functionDeclArgList())

    private val List<FunctionArgument>.getFunctionTargetByAnnotation: AutomatonReference?
        get() {
            val targetArg = firstOrNull { arg ->
                arg.annotationUsages.any { it.annotationReference.name == "target" }
            } ?: return null
            val automatonName = targetArg.typeReference.name
            return AutomatonReferenceBuilder.build(automatonName, functionContext)
        }

    override fun visitEnsuresContract(ctx: EnsuresContractContext) {
        processContract(ctx.name?.text?.extractIdentifier(), ContractKind.ENSURES, ctx.expression())
    }

    override fun visitRequiresContract(ctx: RequiresContractContext) {
        processContract(ctx.name?.text?.extractIdentifier(), ContractKind.REQUIRES, ctx.expression())
    }

    override fun visitAssignsContract(ctx: AssignsContractContext) {
        processContract(ctx.name?.text?.extractIdentifier(), ContractKind.ASSIGNS, ctx.expression())
    }

    private fun processContract(name: String?, kind: ContractKind, expressionContext: ExpressionContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val expression = expressionVisitor.visitExpression(expressionContext)

        val contract = Contract(
            name,
            expression,
            kind,
            posGetter.getCtxPosition(fileName, expressionContext)
        )
        buildingFunction.contracts.add(contract)
    }
}

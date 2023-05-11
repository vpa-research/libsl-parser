package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.*
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.UnspecifiedAutomaton
import org.jetbrains.research.libsl.nodes.*
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
    private lateinit var buildingConstructor: Constructor
    private lateinit var buildingDestructor: Destructor
    private lateinit var buildingProcDecl: ProcDecl

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

        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        val targetAutomatonRef = args.getFunctionTargetByAnnotation ?: automatonReference

        val returnType = ctx.functionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(returnType)
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
            context = functionContext
        )

        super.visitFunctionDecl(ctx)
        parentAutomaton?.localFunctions?.add(buildingFunction)
    }

    override fun visitConstructorDecl(ctx: ConstructorDeclContext) {
        val constructorName = ctx.functionName.text.extractIdentifier()

        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        buildingConstructor = Constructor(
            constructorName,
            args,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            context = functionContext
        )

        super.visitConstructorDecl(ctx)
        parentAutomaton?.constructors?.add(buildingConstructor)
    }

    override fun visitDestructorDecl(ctx: DestructorDeclContext) {
        val destructorName = ctx.functionName.text.extractIdentifier()

        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        buildingDestructor = Destructor(
            destructorName,
            args,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            context = functionContext
        )

        super.visitDestructorDecl(ctx)
        parentAutomaton?.destructors?.add(buildingDestructor)
    }

    override fun visitProcDecl(ctx: ProcDeclContext) {
        val procName = ctx.functionName.text.extractIdentifier()

        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        val returnType = ctx.functionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(returnType)
            context.storeVariable(resultVariable)
        }

        buildingProcDecl = ProcDecl(
            procName,
            args,
            returnType,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            context = functionContext
        )

        super.visitProcDecl(ctx)
        parentAutomaton?.procDeclarations?.add(buildingProcDecl)
    }

    override fun visitFunctionBodyStatements(ctx: FunctionBodyStatementsContext) {
        val statements = when {
            funInitialized() -> buildingFunction.statements
            constructorInitialized() -> buildingConstructor.statements
            destructorInitialized() -> buildingDestructor.statements
            procInitialized() -> buildingProcDecl.statements
            else -> return
        }

        val localVariables = when {
            funInitialized() -> buildingFunction.localVariables
            constructorInitialized() -> buildingConstructor.localVariables
            destructorInitialized() -> buildingDestructor.localVariables
            procInitialized() -> buildingProcDecl.localVariables
            else -> return
        }

        BlockStatementVisitor(functionContext, statements, localVariables).visit(ctx)
    }

    private val FunctionDeclContext.args: List<FunctionArgument>
        get() = this
            .functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->
                val typeRef = processTypeIdentifier(parameter.type)
                val annotationsReferences = getAnnotationUsages(parameter.annotationUsage())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i, annotationsReferences)

                if (annotationsReferences.any { it.annotationReference.name == "Target" }) {
                    val targetAutomatonName = typeRef.name
                    val targetAutomatonReference = AutomatonReferenceBuilder.build(targetAutomatonName, context)
                    arg.targetAutomaton = targetAutomatonReference
                    arg.typeReference = targetAutomatonReference.resolveOrError().typeReference
                }

                arg
            }
            .orEmpty()

    private val ConstructorDeclContext.args: List<FunctionArgument>
        get() = this
            .functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->
                val typeRef = processTypeIdentifier(parameter.type)
                val annotationsReferences = getAnnotationUsages(parameter.annotationUsage())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i, annotationsReferences)
                arg
            }
            .orEmpty()

    private val DestructorDeclContext.args: List<FunctionArgument>
        get() = this
            .functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->
                val typeRef = processTypeIdentifier(parameter.type)
                val annotationsReferences = getAnnotationUsages(parameter.annotationUsage())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i, annotationsReferences)
                arg
            }
            .orEmpty()

    private val ProcDeclContext.args: List<FunctionArgument>
        get() = this
            .functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->
                val typeRef = processTypeIdentifier(parameter.type)
                val annotationsReferences = getAnnotationUsages(parameter.annotationUsage())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i, annotationsReferences)
                arg
            }
            .orEmpty()

    private val List<FunctionArgument>.getFunctionTargetByAnnotation: AutomatonReference?
        get() {
            val targetArg = firstOrNull { arg ->
                arg.annotationUsages.any { it.annotationReference.name == "target"}
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

        val contract = Contract(name, expression, kind)
        if (funInitialized()) buildingFunction.contracts.add(contract)
        if (constructorInitialized()) buildingConstructor.contracts.add(contract)
        if (destructorInitialized()) buildingDestructor.contracts.add(contract)
    }

    private fun funInitialized(): Boolean {
        return this::buildingFunction.isInitialized
    }

    private fun constructorInitialized(): Boolean {
        return this::buildingConstructor.isInitialized
    }

    private fun destructorInitialized(): Boolean {
        return this::buildingDestructor.isInitialized
    }

    private fun procInitialized(): Boolean {
        return this::buildingProcDecl.isInitialized
    }
}

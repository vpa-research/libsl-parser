package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.ConstructorDeclContext
import org.jetbrains.research.libsl.LibSLParser.DestructorDeclContext
import org.jetbrains.research.libsl.LibSLParser.FunctionAnnotationsContext
import org.jetbrains.research.libsl.LibSLParser.ExpressionContext
import org.jetbrains.research.libsl.LibSLParser.FunctionDeclContext
import org.jetbrains.research.libsl.LibSLParser.ProcDeclContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.UnspecifiedAutomaton
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.references.AnnotationReference
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
    private lateinit var buildingProc: Proc

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

        val annotationReferences = getFunctionAnnotationReferenceList(ctx.functionAnnotations())

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
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            targetAutomatonRef = targetAutomatonRef,
            context = functionContext
        )

        super.visitFunctionDecl(ctx)
        parentAutomaton?.localFunctions?.add(buildingFunction)
    }

    override fun visitConstructorDecl(ctx: ConstructorDeclContext) {
        val constructorName = ctx.functionName?.text?.extractIdentifier()

        val annotationReferences = getFunctionAnnotationReferenceList(ctx.functionAnnotations())

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
        val destructorName = ctx.functionName?.text?.extractIdentifier()

        val annotationReferences = getFunctionAnnotationReferenceList(ctx.functionAnnotations())

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

        val annotationReferences = getFunctionAnnotationReferenceList(ctx.functionAnnotations())

        val args = ctx.args.toMutableList()
        args.forEach { arg -> functionContext.storeFunctionArgument(arg) }

        val returnType = ctx.functionType?.let { processTypeIdentifier(it) }

        if (returnType != null) {
            val resultVariable = ResultVariable(returnType)
            context.storeVariable(resultVariable)
        }

        buildingProc = Proc(
            procName,
            args,
            returnType,
            annotationReferences,
            hasBody = ctx.functionBody() != null,
            context = functionContext
        )

        super.visitProcDecl(ctx)
        parentAutomaton?.procList?.add(buildingProc)
    }

    private val FunctionDeclContext.args: List<FunctionArgument>
        get() = this
            .functionDeclArgList()
            ?.parameter()
            ?.mapIndexed { i, parameter ->
                val typeRef = processTypeIdentifier(parameter.type)
                val annotationsReferences = getFunctionParamAnnotationReferenceList(parameter.functionAnnotations())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i, annotationsReferences)

                if (annotationsReferences.any{it.name == "Target"}) {
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
                val annotationsReferences = getFunctionParamAnnotationReferenceList(parameter.functionAnnotations())
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
                val annotationsReferences = getFunctionParamAnnotationReferenceList(parameter.functionAnnotations())
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
                val annotationsReferences = getFunctionParamAnnotationReferenceList(parameter.functionAnnotations())
                val arg = FunctionArgument(parameter.name.text.extractIdentifier(), typeRef, i, annotationsReferences)
                arg
            }
            .orEmpty()

    private fun getFunctionAnnotationReferenceList(ctx: List<FunctionAnnotationsContext>): MutableList<AnnotationReference> {
        val annotationReferenceList = mutableListOf<AnnotationReference>()
        val annotationReferences = ctx.mapNotNull { processFunctionAnnotation(it) }
        annotationReferenceList.addAll(annotationReferences)
        return annotationReferenceList
    }

    private fun processFunctionAnnotation(ctx: FunctionAnnotationsContext?): AnnotationReference? {
        ctx ?: return null
        val name = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(functionContext)
        val args = ctx.expressionsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }.orEmpty().toMutableList()

        context.storeAnnotation(Annotation(name, args))

        return AnnotationReference(name, context)
    }

    private fun getFunctionParamAnnotationReferenceList(ctx: List<FunctionAnnotationsContext>): MutableList<AnnotationReference> {
        val annotationReferenceList = mutableListOf<AnnotationReference>()
        val annotationReferences = ctx.mapNotNull { processFunctionParamAnnotationReference(it) }
        annotationReferenceList.addAll(annotationReferences)
        return annotationReferenceList
    }

    private fun processFunctionParamAnnotationReference(ctx: FunctionAnnotationsContext?): AnnotationReference? {
        ctx ?: return null

        val name = ctx.Identifier().asPeriodSeparatedString()
        val expressionVisitor = ExpressionVisitor(functionContext)
        val args = ctx.expressionsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }.orEmpty().toMutableList()

        context.storeAnnotation(Annotation(name, args))

        if (name == "target") {
            return AnnotationReference("target", context)
        }
        return AnnotationReference(name, context)
    }

    private val List<FunctionArgument>.getFunctionTargetByAnnotation: AutomatonReference?
        get() {
            val targetArg = firstOrNull { arg -> arg.annotationsReferences?.any {
                it.resolveOrError().name == "target"} ?: false } ?: return null
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
        if (funInitialized()) buildingFunction.contracts.add(contract)
        if (constructorInitialized()) buildingConstructor.contracts.add(contract)
        if (destructorInitialized()) buildingDestructor.contracts.add(contract)
        if (procInitialized()) buildingProc.contracts.add(contract)
    }

    override fun visitVariableAssignment(ctx: LibSLParser.VariableAssignmentContext) {
        val expressionVisitor = ExpressionVisitor(functionContext)
        val left = expressionVisitor.visitQualifiedAccess(ctx.qualifiedAccess())
        val value = expressionVisitor.visitAssignmentRight(ctx.assignmentRight())
        val assignment = Assignment(left, value)

        if (funInitialized()) buildingFunction.statements.add(assignment)
        if (constructorInitialized()) buildingConstructor.statements.add(assignment)
        if (destructorInitialized()) buildingDestructor.statements.add(assignment)
        if (procInitialized()) buildingProc.statements.add(assignment)
    }

    override fun visitAction(ctx: LibSLParser.ActionContext) {
        val name = ctx.Identifier().text.extractIdentifier()
        val expressionVisitor = ExpressionVisitor(functionContext)
        val args = ctx.expressionsList().expression().map { expr ->
            expressionVisitor.visitExpression(expr)
        }.toMutableList()

        val action = Action(name, args)

        if (funInitialized()) buildingFunction.statements.add(action)
        if (constructorInitialized()) buildingConstructor.statements.add(action)
        if (destructorInitialized()) buildingDestructor.statements.add(action)
        if (procInitialized()) buildingProc.statements.add(action)
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
        return this::buildingProc.isInitialized
    }
}

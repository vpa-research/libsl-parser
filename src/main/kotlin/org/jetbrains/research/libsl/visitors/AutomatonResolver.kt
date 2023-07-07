package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.UnresolvedState
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.nodes.references.builders.FunctionReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder
import org.jetbrains.research.libsl.utils.EntityPosition
import org.jetbrains.research.libsl.utils.PositionGetter

class AutomatonResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val automatonContext: AutomatonContext
) : LibSLParserVisitor<Unit>(automatonContext) {
    private lateinit var buildingAutomaton: Automaton
    private val fileName = context.fileName
    private val posGetter = PositionGetter()

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val name = ctx.name.asPeriodSeparatedString()
        val typeName = ctx.type.periodSeparatedFullName().asPeriodSeparatedString()
        val typeReference = TypeReferenceBuilder.build(typeName, mutableListOf(), context = context)
        val annotationReferences = getAnnotationUsages(ctx.annotationUsage())

        if (ctx.CONCEPT() == null) {
            buildingAutomaton = Automaton(
                isConcept = false,
                name,
                typeReference,
                annotationReferences,
                context = automatonContext,
                entityPosition = posGetter.getCtxPosition(fileName, ctx)
            )
        } else {
            buildingAutomaton = AutomatonConcept(
                isConcept = true,
                name,
                typeReference,
                annotationReferences,
                context = automatonContext,
                entityPosition = posGetter.getCtxPosition(fileName, ctx)
            )
        }

        super.visitAutomatonDecl(ctx)
        context.parentContext!!.storeAutomata(buildingAutomaton)
    }

    override fun visitImplementedConcepts(ctx: LibSLParser.ImplementedConceptsContext) {
        ctx.concept().forEach {
            buildingAutomaton.implementedConcepts.add(ImplementedConcept(it.name.text, posGetter.getCtxPosition(fileName, ctx)))
        }
    }

    /**
     * Visit constructor arguments
     */
    override fun visitConstructorVariables(ctx: LibSLParser.ConstructorVariablesContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val name = ctx.nameWithType().name.asPeriodSeparatedString()
        val typeReference = processTypeIdentifier(ctx.nameWithType().type)
        val argument = ConstructorArgument(keyword, name, typeReference, getAnnotationUsages(ctx.annotationUsage()),
        posGetter.getCtxPosition(fileName, ctx)
        )
        context.storeVariable(argument)
        buildingAutomaton.constructorVariables.add(argument)
    }

    override fun visitAutomatonStateDecl(ctx: LibSLParser.AutomatonStateDeclContext) {
        val stateKind = StateKind.fromString(ctx.keyword.text)

        val names = ctx.identifierList().Identifier().map { it.text }
        for (name in names) {
            val state = State(name, stateKind, entityPosition = posGetter.getCtxPosition(fileName, ctx))
            buildingAutomaton.states.add(state)
        }
    }

    override fun visitAutomatonShiftDecl(ctx: LibSLParser.AutomatonShiftDeclContext) {
        val toStateName = ctx.to.text
        val toState = getToState(toStateName, ctx)
        if (toState == null) {
            errorManager(UnresolvedState("unresolved state: $toStateName", ctx.position()))
            return
        }

        for (fromStateName in ctx.fromStatesNames) {
            val fromState = getFromState(fromStateName, ctx)
            if (fromState == null) {
                errorManager(UnresolvedState("unresolved state: $fromStateName", ctx.position()))
                continue
            }

            val functionsRefs = ctx.functionsRefs.toMutableList()
            val shift = Shift(fromState, toState, functionsRefs, posGetter.getCtxPosition(fileName, ctx))
            buildingAutomaton.shifts.add(shift)
        }
    }

    private fun getFromState(name: String, ctx: LibSLParser.AutomatonShiftDeclContext): State? {
        if (name == "any") {
            return State(name, StateKind.SIMPLE, isAny = true, entityPosition = posGetter.getCtxPosition(fileName, ctx))
        }

        return buildingAutomaton.states.firstOrNull { s -> s.name == name }
    }

    private fun getToState(name: String, ctx: LibSLParser.AutomatonShiftDeclContext): State? {
        if (name == "self") {
            return State(name, StateKind.SIMPLE, isSelf = true, entityPosition = posGetter.getCtxPosition(fileName, ctx))
        }

        return buildingAutomaton.states.firstOrNull { s -> s.name == name }
    }

    private val LibSLParser.AutomatonShiftDeclContext.fromStatesNames: List<String>
        get() {
            return if (this.identifierList() != null) {
                identifierList().Identifier().map { id -> id.text }
            } else {
                listOf(Identifier().first().text)
            }
        }

    private val LibSLParser.AutomatonShiftDeclContext.functionsRefs: List<FunctionReference>
        get() {
            this.functionsList() ?: functionsListPart() ?: return listOf()

            val result = mutableListOf<FunctionReference>()

            for (functionListPart in this.functionsList()?.functionsListPart() ?: listOf(functionsListPart())) {
                val ids = functionListPart.Identifier().map { id -> id.asPeriodSeparatedString() }
                val functionName = ids.first()

                val argTypesNames = ids.drop(1)
                val argTypesRefs = argTypesNames.map { name ->
                    TypeReferenceBuilder.build(name, mutableListOf(), context = context)
                }

                val ref = FunctionReferenceBuilder.build(
                    name = functionName,
                    argTypes = argTypesRefs,
                    context
                )

                result.add(ref)
            }

            return result
        }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        val keyword = VariableKind.fromString(ctx.keyword.text)
        val name = ctx.nameWithType().name.asPeriodSeparatedString()
        val typeReference = processTypeIdentifier(ctx.nameWithType().type)
        val expressionVisitor = ExpressionVisitor(context)
        val initValue = ctx.expression()?.let { right -> expressionVisitor.visitExpression(right) }

        val variable = VariableWithInitialValue(
            keyword,
            name,
            typeReference,
            getAnnotationUsages(ctx.annotationUsage()),
            initValue,
            posGetter.getCtxPosition(fileName, ctx)
        )
        buildingAutomaton.internalVariables.add(variable)
        context.storeVariable(variable)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val functionContext = FunctionContext(context)
        FunctionVisitor(functionContext, buildingAutomaton, errorManager).visitFunctionDecl(ctx)
    }

    override fun visitConstructorDecl(ctx: LibSLParser.ConstructorDeclContext) {
        val functionContext = FunctionContext(context)
        FunctionVisitor(functionContext, buildingAutomaton, errorManager).visitConstructorDecl(ctx)
    }

    override fun visitDestructorDecl(ctx: LibSLParser.DestructorDeclContext) {
        val functionContext = FunctionContext(context)
        FunctionVisitor(functionContext, buildingAutomaton, errorManager).visitDestructorDecl(ctx)
    }

    override fun visitProcDecl(ctx: LibSLParser.ProcDeclContext) {
        val functionContext = FunctionContext(context)
        FunctionVisitor(functionContext, buildingAutomaton, errorManager).visitProcDecl(ctx)
    }
}

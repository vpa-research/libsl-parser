package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.NameWithTypeContext
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.UnresolvedState
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.nodes.references.builders.FunctionReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder

class AutomatonResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val automatonContext: AutomatonContext
) : LibSLParserVisitor<Unit>(automatonContext) {
    private lateinit var buildingAutomaton: Automaton
    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val name = ctx.name.asPeriodSeparatedString()
        val typeName = ctx.type.asPeriodSeparatedString()
        val typeReference = TypeReferenceBuilder.build(typeName, context = context)

        buildingAutomaton = Automaton(
            name,
            typeReference,
            context = automatonContext
        )

        super.visitAutomatonDecl(ctx)
        context.parentContext!!.storeAutomata(buildingAutomaton)
    }

    /**
     * Visit constructor arguments
     */
    override fun visitNameWithType(ctx: NameWithTypeContext) {
        val name = ctx.name.asPeriodSeparatedString()
        val typeReference = processTypeIdentifier(ctx.typeIdentifier())
        val argument = ConstructorArgument(name, typeReference)

        buildingAutomaton.constructorVariables.add(argument)
    }

    override fun visitAutomatonStateDecl(ctx: LibSLParser.AutomatonStateDeclContext) {
        val stateKind = StateKind.fromString(ctx.keyword.text)

        val names = ctx.identifierList().Identifier().map { it.text }
        for (name in names) {
            val state = State(name, stateKind)
            buildingAutomaton.states.add(state)
        }
    }

    override fun visitAutomatonShiftDecl(ctx: LibSLParser.AutomatonShiftDeclContext) {
        val toStateName = ctx.to.text
        val toState = getToState(toStateName)
        if (toState == null) {
            errorManager(UnresolvedState("unresolved state: $toStateName", ctx.position()))
            return
        }

        for (fromStateName in ctx.fromStatesNames) {
            val fromState = getFromState(fromStateName)
            if (fromState == null) {
                errorManager(UnresolvedState("unresolved state: $fromStateName", ctx.position()))
                continue
            }

            val functionsRefs = ctx.functionsRefs.toMutableList()
            val shift = Shift(fromState, toState, functionsRefs)
            buildingAutomaton.shifts.add(shift)
        }
    }

    private fun getFromState(name: String): State? {
        if (name == "any") {
            return State(name, StateKind.SIMPLE, isAny = true)
        }

        return buildingAutomaton.states.firstOrNull { s -> s.name == name }
    }

    private fun getToState(name: String): State? {
        if (name == "self") {
            return State(name, StateKind.SIMPLE, isSelf = true)
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
            this.functionsList() ?: return listOf()

            val result = mutableListOf<FunctionReference>()

            for (functionListPart in this.functionsList().functionsListPart()) {
                val ids = functionListPart.Identifier().map { id -> id.asPeriodSeparatedString() }
                val functionName = ids.first()

                val argTypesNames = ids.drop(1)
                val argTypesRefs = argTypesNames.map { name ->
                    TypeReferenceBuilder.build(name, context = context)
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

        val variable = VariableWithInitialValue(name, typeReference, initValue)
        buildingAutomaton.internalVariables.add(variable)
        context.storeVariable(variable)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val functionContext = FunctionContext(context)
        FunctionVisitor(functionContext, buildingAutomaton, errorManager).visitFunctionDecl(ctx)
    }

}

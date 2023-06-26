package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.references.*
import org.jetbrains.research.libsl.type.RealType
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.type.TypeInferrer

abstract class LslContextBase {
    abstract val parentContext: LslContextBase?

    private val annotations = mutableListOf<Annotation>()
    private val actions = mutableListOf<Action>()
    private val automata = mutableListOf<Automaton>()
    private val types = mutableListOf<Type>()
    private val functions = mutableListOf<Function>()
    private val procedures = mutableListOf<Procedure>()
    private val variables = mutableListOf<Variable>()

    @Suppress("LeakingThis")
    val typeInferrer = TypeInferrer(this)

    fun storeAutomata(automaton: Automaton) {
        automata.add(automaton)
    }

    fun storeType(type: Type) {
        if (type in types)
            return

        // hack to kick out real types when semantic type is being resolved after reference to it
        if (type !is RealType) {
            types.removeIf { storedType -> storedType.name == type.name && storedType is RealType }
        }

        types.add(type)
    }

    fun storeFunction(function: Function) {
        functions.add(function)
    }

    fun storeVariable(variable: Variable) {
        variables.add(variable)
    }

    fun storeAnnotation(annotation: Annotation) {
        annotations.add(annotation)
    }

    fun storeDeclaredAction(action: Action) {
        actions.add(action)
    }

    open fun resolveAutomaton(reference: AutomatonReference): Automaton? {
        return automata.firstOrNull { automaton -> reference.isReferenceMatchWithNode(automaton) }
            ?: parentContext?.resolveAutomaton(reference)
    }

    open fun resolveType(reference: TypeReference): Type? {
        return types.firstOrNull { types -> reference.isReferenceMatchWithNode(types) }
            ?: parentContext?.resolveType(reference)
    }

    open fun resolveFunction(reference: FunctionReference): Function? {
        return functions.firstOrNull { function -> reference.isReferenceMatchWithNode(function) }
            ?: parentContext?.resolveFunction(reference)
    }

    open fun resolveVariable(reference: VariableReference): Variable? {
        return variables.firstOrNull { variable -> reference.isReferenceMatchWithNode(variable) }
            ?: parentContext?.resolveVariable(reference)
    }

    open fun resolveAnnotation(reference: AnnotationReference): Annotation? {
        return annotations.firstOrNull { annotation -> reference.isReferenceMatchWithNode(annotation) }
            ?: parentContext?.resolveAnnotation(reference)
    }

    open fun resolveAction(reference: ActionReference): Action? {
        return actions.firstOrNull { action -> reference.isReferenceMatchWithNode(action) }
            ?: parentContext?.resolveAction(reference)
    }

    internal fun getAllTypes() = types

    internal fun getAllAutomata() = automata

    internal fun getAllFunctions() = functions

    internal fun getAllVariables() = variables

    internal fun getAllAnnotations() = annotations

    internal fun getAllDeclaredActions() = actions
}

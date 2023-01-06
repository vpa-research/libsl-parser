package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.Variable
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.VariableReference
import org.jetbrains.research.libsl.type.RealType
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.type.TypeInferer

abstract class LslContextBase {
    abstract val parentContext: LslContextBase?

    private val automata = mutableListOf<Automaton>()
    private val types = mutableListOf<Type>()
    private val functions = mutableListOf<Function>()
    private val variables = mutableListOf<Variable>()

    @Suppress("LeakingThis")
    val typeInferer = TypeInferer(this)

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

    internal fun getAllTypes() = types

    internal fun getAllAutomata() = automata

    internal fun getAllFunctions() = functions

    internal fun getAllVariables() = variables
}

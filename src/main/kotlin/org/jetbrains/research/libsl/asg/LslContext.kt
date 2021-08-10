package org.jetbrains.research.libsl.asg

class LslContext {
    private val typeStorage = mutableMapOf<String, Type>()
    private val functionStorage = mutableMapOf<String, MutableList<Function>>()
    private val automatonStorage = mutableMapOf<String, Automaton>()
    val globalVariables = mutableMapOf<String, Variable>()

    fun storeResolvedType(type: Type) {
        typeStorage[type.semanticType] = type
    }

    fun resolveType(name: String) = typeStorage[name]

    fun storeResolvedFunction(function: Function) {
        functionStorage.getOrPut(function.name) { mutableListOf() }.add(function)
    }

    fun resolveFunction(
        name: String,
        automatonName: String?,
        args: List<Argument>? = null,
        argsType: List<Type>? = null,
        returnType: Type? = null
    ) = functionStorage[name]
        ?.asSequence()
        ?.filter { it.automatonName == automatonName }
        ?.filter { if (args != null) it.args == args else true }
        ?.filter { if (argsType != null) it.args.map { arg -> arg.type }.toList() == argsType else true }
        ?.filter { if (returnType != null) it.returnType == returnType else true }
        ?.firstOrNull()

    fun storeResolvedAutomaton(automaton: Automaton) {
        automatonStorage[automaton.name] = automaton
    }

    fun resolveAutomaton(name: String): Automaton? {
        return automatonStorage[name]
    }

    fun storeGlobalVariable(variable: Variable) {
        globalVariables[variable.name] = variable
    }

    fun resolveVariable(name: String): Variable? {
        return globalVariables[name]
    }
}
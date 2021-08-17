package org.jetbrains.research.libsl.asg

class LslContext {
    val typeStorage = mutableMapOf<String, Type>()
    private val functionStorage = mutableMapOf<String, MutableList<Function>>()
    private val automatonStorage = mutableMapOf<String, Automaton>()
    val globalVariables = mutableMapOf<String, GlobalVariableDeclaration>()

    private val importedContexts = mutableListOf<LslContext>()

    fun init() {
        val types = listOf(
            IntType(this, IntType.IntCapacity.INT8),
            IntType(this, IntType.IntCapacity.INT16),
            IntType(this, IntType.IntCapacity.INT32),
            IntType(this, IntType.IntCapacity.INT64),

            UnsignedType(this, UnsignedType.UnsignedCapacity.UNSIGNED8),
            UnsignedType(this, UnsignedType.UnsignedCapacity.UNSIGNED16),
            UnsignedType(this, UnsignedType.UnsignedCapacity.UNSIGNED32),
            UnsignedType(this, UnsignedType.UnsignedCapacity.UNSIGNED64),

            FloatType(this, FloatType.FloatCapacity.FLOAT32),
            FloatType(this, FloatType.FloatCapacity.FLOAT64),

            BoolType(this),

            CharType(this),

            StringType(this)
        )

        types.forEach(::storeResolvedType)
    }

    fun storeResolvedType(type: Type) {
        typeStorage[type.fullName] = type
    }

    fun resolveType(name: String): Type? = typeStorage[name] ?: importedContexts.firstNotNullOfOrNull { it.resolveType(name) }

    fun storeResolvedFunction(function: Function) {
        functionStorage.getOrPut(function.name) { mutableListOf() }.add(function)
    }

    fun resolveFunction(
        name: String,
        automatonName: String?,
        args: List<FunctionArgument>? = null,
        argsType: List<Type>? = null,
        returnType: Type? = null
    ): Function? = functionStorage[name]
        ?.asSequence()
        ?.filter { it.automatonName == automatonName }
        ?.filter { if (args != null) it.args == args else true }
        ?.filter { if (argsType != null) it.args.map { arg -> arg.type }.toList() == argsType else true }
        ?.filter { if (returnType != null) it.returnType == returnType else true }
        ?.firstOrNull()
        ?: importedContexts.firstNotNullOfOrNull { it.resolveFunction(name, automatonName, args, argsType, returnType) }

    fun storeResolvedAutomaton(automaton: Automaton) {
        automatonStorage[automaton.name] = automaton
    }

    fun resolveAutomaton(name: String): Automaton? = automatonStorage[name]
        ?: importedContexts.firstNotNullOfOrNull { it.resolveAutomaton(name) }

    fun storeGlobalVariable(variable: GlobalVariableDeclaration) {
        globalVariables[variable.name] = variable
    }

    fun resolveVariable(name: String): GlobalVariableDeclaration? = globalVariables[name]
        ?: importedContexts.firstNotNullOfOrNull { it.resolveVariable(name) }

    fun import(context: LslContext) {
        importedContexts.add(context)
    }
}
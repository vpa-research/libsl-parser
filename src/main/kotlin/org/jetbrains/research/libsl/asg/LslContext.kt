package org.jetbrains.research.libsl.asg

class LslContext {
    val typeStorage = mutableMapOf<String, Type>()
    private val functionStorage = mutableMapOf<String, MutableList<Function>>()
    private val automatonStorage = mutableMapOf<String, Automaton>()
    val globalVariables = mutableMapOf<String, GlobalVariableDeclaration>()

    private val importedContexts = mutableListOf<LslContext>()

    fun init() {
        val types = buildList<Type> {
            for (pointer in listOf(true, false)) {
                add(IntType(this@LslContext, IntType.IntCapacity.INT8, pointer))
                add(IntType(this@LslContext, IntType.IntCapacity.INT16, pointer))
                add(IntType(this@LslContext, IntType.IntCapacity.INT32, pointer))
                add(IntType(this@LslContext, IntType.IntCapacity.INT64, pointer))

                add(UnsignedType(this@LslContext, UnsignedType.UnsignedCapacity.UNSIGNED8, pointer))
                add(UnsignedType(this@LslContext, UnsignedType.UnsignedCapacity.UNSIGNED16, pointer))
                add(UnsignedType(this@LslContext, UnsignedType.UnsignedCapacity.UNSIGNED32, pointer))
                add(UnsignedType(this@LslContext, UnsignedType.UnsignedCapacity.UNSIGNED64, pointer))

                add(FloatType(this@LslContext, FloatType.FloatCapacity.FLOAT32, pointer))
                add(FloatType(this@LslContext, FloatType.FloatCapacity.FLOAT64, pointer))

                add(BoolType(this@LslContext, pointer))
                add(CharType(this@LslContext, pointer))
                add(StringType(this@LslContext, pointer))
                add(VoidType(this@LslContext, pointer))
            }
        }

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

    fun storeGlobalVariableDeclaration(variableDeclaration: GlobalVariableDeclaration) {
        globalVariables[variableDeclaration.variable.name] = variableDeclaration
    }

    fun resolveGlobalVariable(name: String): GlobalVariableDeclaration? = globalVariables[name]
        ?: importedContexts.firstNotNullOfOrNull { it.resolveGlobalVariable(name) }

    fun import(context: LslContext) {
        importedContexts.add(context)
    }
}
package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.FunctionArgument

class FunctionContext(
    override val parentContext: LslContextBase
) : LslContextBase() {
    private val functionArguments = mutableListOf<FunctionArgument>()

    fun resolveFunctionArgumentByName(name: String): FunctionArgument? {
        return functionArguments.firstOrNull { arg -> arg.name == name }
    }

    fun storeFunctionArgument(arg: FunctionArgument) {
        functionArguments.add(arg)
    }
}

package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.FunctionArgument
import org.jetbrains.research.libsl.nodes.ResultVariable
import org.jetbrains.research.libsl.nodes.Variable
import org.jetbrains.research.libsl.nodes.references.VariableReference

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

    override fun resolveVariable(reference: VariableReference): Variable? {
        return functionArguments.firstOrNull { it.name == reference.name } ?: super.resolveVariable(reference)
    }
}

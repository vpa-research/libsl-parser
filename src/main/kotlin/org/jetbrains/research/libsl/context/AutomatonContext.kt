package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.VariableDeclaration

class AutomatonContext(
    override val parentContext: LslContextBase
) : LslContextBase() {
    private val automatonVariables = mutableListOf<VariableDeclaration>()

    fun resolveAutomatonVariableByName(name: String): VariableDeclaration? {
        return automatonVariables.firstOrNull { arg -> arg.name == name }
    }

    fun storeAutomatonVariable(variable: VariableDeclaration) {
        automatonVariables.add(variable)
    }
}

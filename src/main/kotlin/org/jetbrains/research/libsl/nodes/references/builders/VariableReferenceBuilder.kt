package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.Variable
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.VariableReference

object VariableReferenceBuilder {
    fun build(
        name: String,
        context: LslContextBase
    ): VariableReference {
        return VariableReference(name, context)
    }

    fun Variable.getReference(context: LslContextBase): VariableReference {
        return build(this.name, context)
    }
}

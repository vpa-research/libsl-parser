package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.references.AutomatonReference

object AutomatonReferenceBuilder {
    fun build(
        name: String,
        context: LslContextBase
    ): AutomatonReference {
        return AutomatonReference(name, context)
    }

    fun Automaton.getReference(context: LslContextBase): AutomatonReference {
        return build(this.name, context)
    }
}

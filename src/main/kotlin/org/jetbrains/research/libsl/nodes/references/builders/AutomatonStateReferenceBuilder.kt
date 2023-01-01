package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.State
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.AutomatonStateReference
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder.getReference

object AutomatonStateReferenceBuilder {
    fun build(
        name: String,
        automatonReference: AutomatonReference,
        context: LslContextBase
    ): AutomatonStateReference {
        return AutomatonStateReference(name, automatonReference, context)
    }

    fun State.getReference(context: LslContextBase): AutomatonStateReference {
        return build(this.name, this.automaton.getReference(context), context)
    }
}

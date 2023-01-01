package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.State

class AutomatonStateReference(
    val name: String,
    private val automatonReference: AutomatonReference,
    override val context: LslContextBase
) : LslReference<State, AutomatonStateReference> {
    override fun resolve(): State? {
        val automaton = automatonReference.resolve() ?: return null
        return automaton.states.firstOrNull { state -> state.name == name }
    }

    override fun isSameReference(other: AutomatonStateReference): Boolean {
        if (other.name != name)
            return false

        if (!other.automatonReference.isSameReference(this.automatonReference))
            return false

        return true
    }

    override fun isReferenceMatchWithNode(node: State): Boolean {
        return node.name == name && automatonReference.isReferenceMatchWithNode(node.automaton)
    }
}

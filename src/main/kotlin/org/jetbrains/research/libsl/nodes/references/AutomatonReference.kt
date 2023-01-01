package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Automaton

class AutomatonReference(
    val name: String,
    override val context: LslContextBase
) : LslReference<Automaton, AutomatonReference> {
    override fun resolve(): Automaton? {
        return context.resolveAutomaton(this)
    }

    override fun isReferenceMatchWithNode(node: Automaton): Boolean {
        return node.name == name
    }

    override fun isSameReference(other: AutomatonReference): Boolean {
        return this.name == other.name
    }

    override fun toString(): String {
        return "AutomatonReference($name)"
    }
}

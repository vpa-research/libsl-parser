package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Variable

data class VariableReference(
    val name: String,
    override val context: LslContextBase
) : LslReference<Variable, VariableReference> {
    override fun resolve(): Variable? {
        return context.resolveVariable(this)
    }

    override fun isReferenceMatchWithNode(node: Variable): Boolean {
        if (node.name != this.name) {
            return false
        }

        return true
    }

    override fun isSameReference(other: VariableReference): Boolean {
        return this.name == other.name
    }

    override fun toString(): String {
        return "VariableReference(name=$name)"
    }
}

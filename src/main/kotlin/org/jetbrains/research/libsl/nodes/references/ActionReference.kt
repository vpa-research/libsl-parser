package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Action
import org.jetbrains.research.libsl.nodes.ActionUsage

data class ActionReference(
    val name: String,
    val argTypes: List<TypeReference>,
    override val context: LslContextBase
) : LslReference<Action, ActionReference> {
    override fun resolve(): Action? {
        return context.resolveAction(this)
    }

    // todo: Improve a support for overloading
    override fun isSameReference(other: ActionReference): Boolean {
        return other.name == this.name && other.argTypes.size == this.argTypes.size
    }

    // todo: Improve a support for overloading
    override fun isReferenceMatchWithNode(node: Action): Boolean {
        return this.name == node.name && this.argTypes.size == node.argumentDescriptors.size
    }

    override fun toString(): String {
        return "ActionReference($name)"
    }
}

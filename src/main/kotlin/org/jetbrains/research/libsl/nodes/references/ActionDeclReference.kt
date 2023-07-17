package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.ActionDecl

data class ActionDeclReference(
    val name: String,
    val paramTypes: List<TypeReference>,
    override val context: LslContextBase
) : LslReference<ActionDecl, ActionDeclReference> {
    override fun resolve(): ActionDecl? {
        return context.resolveDeclaredAction(this)
    }

    // todo: Improve a support for overloading
    override fun isSameReference(other: ActionDeclReference): Boolean {
        return other.name == this.name && other.paramTypes.size == this.paramTypes.size
    }

    // todo: Improve a support for overloading
    override fun isReferenceMatchWithNode(node: ActionDecl): Boolean {
        return this.name == node.name && this.paramTypes.size == node.argumentDescriptors.size
    }

    override fun toString(): String {
        return "ActionReference($name)"
    }
}

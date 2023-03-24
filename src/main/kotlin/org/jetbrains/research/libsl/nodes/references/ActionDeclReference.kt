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

    override fun isReferenceMatchWithNode(node: ActionDecl): Boolean {
        if (node.name != this.name) {
            return false
        }

        if (!areParamsMatch(node.values.map { value -> value.typeReference })) {
            return false
        }

        return true
    }

    override fun isSameReference(other: ActionDeclReference): Boolean {
        return this.name == other.name && areParamsMatch(other.paramTypes)
    }

    private fun areParamsMatch(params: List<TypeReference>): Boolean {
        if (params.size != this.paramTypes.size) {
            return false
        }

        return params.withIndex().all { (i, a) -> a.isSameReference(params[i]) }
    }

    override fun toString(): String {
        return "ActionReference($name)"
    }
}
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

        if (!doParamsMatch(node.values.map { value -> value.typeReference })) {
            return false
        }

        return true
    }

    override fun isSameReference(other: ActionDeclReference): Boolean {
        return this.name == other.name && doParamsMatch(other.paramTypes)
    }

    private fun doParamsMatch(params: List<TypeReference>): Boolean {
        if (params.size != this.paramTypes.size) {
            return false
        }

        return params.withIndex().all { (index, element) -> element.isSameReference(params[index]) }
    }

    override fun toString(): String {
        return "ActionReference(name: $name, paramTypes:$paramTypes)"
    }
}

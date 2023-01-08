package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Function

data class FunctionReference(
    val name: String,
    val argTypes: List<TypeReference>,
    override val context: LslContextBase
) : LslReference<Function, FunctionReference> {
    override fun resolve(): Function? {
        return context.resolveFunction(this)
    }

    override fun isReferenceMatchWithNode(node: Function): Boolean {
        if (node.name != this.name) {
            return false
        }

        if (!areArgsMatch(node.args.map { arg -> arg.typeReference })) {
            return false
        }

        return true
    }

    override fun isSameReference(other: FunctionReference): Boolean {
        return this.name == other.name && areArgsMatch(other.argTypes)
    }

    private fun areArgsMatch(args: List<TypeReference>): Boolean {
        if (args.size != this.argTypes.size) {
            return false
        }

        return args.withIndex().all { (i, a) -> a.isSameReference(args[i]) }
    }

    override fun toString(): String {
        return "FunctionReference($name)"
    }
}
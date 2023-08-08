package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.type.ArrayType
import org.jetbrains.research.libsl.type.Type

data class TypeReference(
    val name: String,
    val isPointer: Boolean,
    val genericReference: TypeReference?,
    override val context: LslContextBase
) : LslReference<Type, TypeReference> {
    override fun resolve(): Type? {
        return resolveArrayType() ?: context.resolveType(this)
    }

    private fun resolveArrayType(): ArrayType? {
        if (name != "array")
            return null
        genericReference!!.resolve() ?: return null
        return ArrayType(isPointer, genericReference, context)
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        if (this.name != node.name) {
            return false
        }

        if (this.isPointer != node.isPointer) {
            return false
        }

        if (!areGenericsMatch(node.generic)) {
            return false
        }

        return true
    }

    private fun areGenericsMatch(otherGenericType: TypeReference?): Boolean {
        if (this.genericReference == null && otherGenericType == null) {
            return true
        }

        if (this.genericReference == null || otherGenericType == null) {
            return false
        }

        return true
    }

    override fun isSameReference(other: TypeReference): Boolean {
        return this.name == other.name
                && this.isPointer == other.isPointer
                && (other.genericReference == null || this.genericReference?.isSameReference(other.genericReference) != false)
    }

    override fun toString(): String {
        return "TypeReference(name=$name, isPointer=$isPointer, genericReference=$genericReference)"
    }
}

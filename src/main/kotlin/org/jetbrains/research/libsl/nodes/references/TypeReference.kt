package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.type.ArrayType
import org.jetbrains.research.libsl.type.ListType
import org.jetbrains.research.libsl.type.NullType
import org.jetbrains.research.libsl.type.Type

open class TypeReference(
    val name: String,
    val isPointer: Boolean,
    val genericReferences: MutableList<TypeReference>,
    override val context: LslContextBase
) : LslReference<Type, TypeReference> {
    override fun resolve(): Type? {
        return resolveArrayType() ?: resolveListType() ?: resolveNullType() ?: context.resolveType(this)
    }

    private fun resolveArrayType(): ArrayType? {
        if (name != "array")
            return null
        genericReferences.forEach { it.resolve() }
        return ArrayType(isPointer, genericReferences, context)
    }

    private fun resolveListType(): ListType? {
        if (name != "list")
            return null
        genericReferences.forEach { it.resolve() }
        return ListType(isPointer, genericReferences, context)
    }

    private fun resolveNullType(): NullType? {
        if (name != "null") {
            return null
        }
        return NullType(false, mutableListOf(), context)
    }

    override fun isReferenceMatchWithNode(node: Type): Boolean {
        if (this.name != node.name) {
            return false
        }

        if (this.isPointer != node.isPointer) {
            return false
        }

        if (!areGenericsMatch(node.generics)) {
            return false
        }

        return true
    }

    private fun areGenericsMatch(generics: MutableList<TypeReference>): Boolean {
        if (this.genericReferences.isEmpty() && generics.isEmpty()) {
            return true
        }

        if (this.genericReferences.isEmpty() || generics.isEmpty()) {
            return false
        }

        return true
    }

    override fun isSameReference(other: TypeReference): Boolean {
        return this.name == other.name
                && this.isPointer == other.isPointer
                && (other.genericReferences.isEmpty() || this.genericReferences.isSameReference(other.genericReferences))
    }

    private fun MutableList<TypeReference>.isSameReference(other: MutableList<TypeReference>): Boolean {
        if (this.size != other.size) {
            return false
        }

        for (i in this.indices) {
            if (!this[i].isSameReference(other[i])) {
                return false
            }
        }

        return true
    }

    override fun toString(): String {
        return "TypeReference(name=$name, isPointer=$isPointer, genericReferences=$genericReferences)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeReference) return false

        if (name != other.name) return false
        if (isPointer != other.isPointer) return false
        if (genericReferences != other.genericReferences) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isPointer.hashCode()
        result = 31 * result + (genericReferences.hashCode() ?: 0)
        result = 31 * result + context.hashCode()
        return result
    }
}

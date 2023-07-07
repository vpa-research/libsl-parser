package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Generic
import org.jetbrains.research.libsl.type.ArrayType
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.PositionGetter

data class TypeReference(
    val name: String,
    val isPointer: Boolean,
    val generics: MutableList<Generic>,
    override val context: LslContextBase
) : LslReference<Type, TypeReference> {
    override fun resolve(): Type? {
        return resolveArrayType() ?: context.resolveType(this)
    }

    private fun resolveArrayType(): ArrayType? {
        if (name != "array")
            return null
        generics.forEach { it.typeReference.resolve() }
        return ArrayType(isPointer, generics, context)
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

    private fun areGenericsMatch(generics: MutableList<Generic>): Boolean {
        if (this.generics.isEmpty() && generics.isEmpty()) {
            return true
        }

        if (this.generics.isEmpty() || generics.isEmpty()) {
            return false
        }

        return true
    }

    override fun isSameReference(other: TypeReference): Boolean {
        val thisGenRefs = this.generics.map { generic -> generic.typeReference }
        val otherGenRefs = other.generics.map { generic -> generic.typeReference }


        return this.name == other.name
                && this.isPointer == other.isPointer
                && (thisGenRefs.isEmpty() || thisGenRefs.isSameReference(otherGenRefs))
    }

    private fun List<TypeReference>.isSameReference(other: List<TypeReference>): Boolean {
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
        return "TypeReference(name=$name, isPointer=$isPointer, genericReferences=$generics)"
    }
}

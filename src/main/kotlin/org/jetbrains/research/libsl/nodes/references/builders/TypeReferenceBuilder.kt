package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type

object TypeReferenceBuilder {
    fun build(
        name: String,
        genericReferences: MutableList<TypeReference>,
        isPointer: Boolean = false,
        context: LslContextBase
    ): TypeReference {
        return TypeReference(name, isPointer, genericReferences, context)
    }

    fun Type.getReference(context: LslContextBase): TypeReference {
        return build(this.name, this.generics, this.isPointer, context)
    }
}

package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.ActionDecl
import org.jetbrains.research.libsl.nodes.references.ActionReference
import org.jetbrains.research.libsl.nodes.references.TypeReference

object ActionReferenceBuilder {
    fun build(
        name: String,
        paramTypes: List<TypeReference>,
        context: LslContextBase
    ): ActionReference {
        return ActionReference(name, paramTypes, context)
    }

    fun ActionDecl.getReference(context: LslContextBase): ActionReference {
        return build(this.name, this.values.map { value -> value.typeReference }, context)
    }
}

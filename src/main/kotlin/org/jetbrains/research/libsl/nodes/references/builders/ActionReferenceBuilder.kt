package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Action
import org.jetbrains.research.libsl.nodes.references.ActionReference
import org.jetbrains.research.libsl.nodes.references.TypeReference

object ActionReferenceBuilder {
    fun build(
        name: String,
        argTypes: List<TypeReference>,
        context: LslContextBase
    ): ActionReference {
        return ActionReference(name, argTypes, context)
    }

    fun Action.getReference(context: LslContextBase): ActionReference {
        return build(this.name, this.argumentDescriptors.map { descr -> descr.typeReference }, context)
    }
}

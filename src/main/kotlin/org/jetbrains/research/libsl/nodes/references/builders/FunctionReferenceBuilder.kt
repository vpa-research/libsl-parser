package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.nodes.references.TypeReference

object FunctionReferenceBuilder {
    fun build(
        name: String?,
        argTypes: List<TypeReference>,
        context: LslContextBase
    ): FunctionReference {
        if(name == null) {
            return FunctionReference("undefined", argTypes, context)
        }
        return FunctionReference(name, argTypes, context)
    }

    fun Function.getReference(context: LslContextBase): FunctionReference {
        return build(this.name, this.args.map { arg -> arg.typeReference }, context)
    }
}

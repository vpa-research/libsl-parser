package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.type.Type

object AnnotationReferenceBuilder {
    fun build(
        name: String,
        types: MutableList<Type>,
        context: LslContextBase
    ): AnnotationReference {
        return AnnotationReference(name, types, context)
    }

    fun Annotation.getReference(context: LslContextBase): AnnotationReference {
        val types = this.values.orEmpty().map { param -> param.typeReference.resolveOrError() }.toMutableList()
        return build(this.name, types, context)
    }
}

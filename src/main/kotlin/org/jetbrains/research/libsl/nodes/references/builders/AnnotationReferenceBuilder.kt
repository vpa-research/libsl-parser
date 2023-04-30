package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.TypeReference

object AnnotationReferenceBuilder {
    fun build(
        name: String,
        argTypes: List<TypeReference>,
        context: LslContextBase
    ): AnnotationReference {
        return AnnotationReference(name, argTypes, context)
    }

    fun Annotation.getReference(context: LslContextBase): AnnotationReference {
        return build(this.name, this.argumentDescriptors.map { descr -> descr.typeReference }, context)
    }
}

package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.references.AnnotationReference

object AnnotationReferenceBuilder {

    fun build(
        name: String,
        context: LslContextBase
    ): AnnotationReference {
        return AnnotationReference(name, context)
    }

    fun Annotation.getReference(context: LslContextBase): AnnotationReference {
        return build(this.name, context)
    }
}
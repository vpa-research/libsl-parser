package org.jetbrains.research.libsl.nodes.references.builders

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.DeclaredAnnotation
import org.jetbrains.research.libsl.nodes.references.DeclaredAnnotationReference

object DeclaredAnnotationReferenceBuilder {

    fun build(
        name: String,
        context: LslContextBase
    ): DeclaredAnnotationReference {
        return DeclaredAnnotationReference(name, context)
    }

    fun DeclaredAnnotation.getReference(context: LslContextBase): DeclaredAnnotationReference {
        return build(this.name, context)
    }
}
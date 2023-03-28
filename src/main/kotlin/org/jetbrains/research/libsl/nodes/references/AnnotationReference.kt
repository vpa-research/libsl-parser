package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.type.Type

data class AnnotationReference(
    val name: String,
    val params: MutableList<Type>,
    override val context: LslContextBase
) : LslReference<Annotation, AnnotationReference> {
    override fun resolve(): Annotation? {
        return context.resolveAnnotation(this)
    }

    override fun isSameReference(other: AnnotationReference): Boolean {
        return other.name == name
    }

    override fun isReferenceMatchWithNode(node: Annotation): Boolean {
        // TODO
        return this.name == node.name
    }

    override fun toString(): String {
        return "AnnotationReference($name)"
    }
}

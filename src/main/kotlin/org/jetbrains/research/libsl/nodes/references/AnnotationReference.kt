package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation

data class AnnotationReference (
    val name: String,
    override val context: LslContextBase
) : LslReference<Annotation, AnnotationReference> {
    override fun resolve(): Annotation? {
        return context.resolveAnnotation(this)
    }

    override fun isSameReference(node: AnnotationReference): Boolean {
        return node.name == name
    }

    override fun isReferenceMatchWithNode(node: Annotation): Boolean {
        return this.name == node.name
    }

    override fun toString(): String {
        return "AnnotationReference($name)"
    }

}
package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.DeclaredAnnotation

data class AnnotationReference (
    val name: String,
    override val context: LslContextBase
) : LslReference<Annotation, AnnotationReference> {
    override fun resolve(): Annotation? {
        return context.resolveAnnotation(this)
    }

    override fun isSameReference(other: AnnotationReference): Boolean {
        return other.name == name
    }

    override fun isReferenceMatchWithNode(node: Annotation): Boolean {
        return this.name == node.name
    }

    override fun toString(): String {
        return "AnnotationReference($name)"
    }
}

data class DeclaredAnnotationReference (
    val name: String,
    override val context: LslContextBase
) : LslReference<DeclaredAnnotation, DeclaredAnnotationReference> {
    override fun resolve(): DeclaredAnnotation? {
        return context.resolveDeclaredAnnotation(this)
    }

    override fun isSameReference(other: DeclaredAnnotationReference): Boolean {
        return other.name == name
    }

    override fun isReferenceMatchWithNode(node: DeclaredAnnotation): Boolean {
        return this.name == node.name
    }

    override fun toString(): String {
        return "DeclaredAnnotationReference($name)"
    }
}


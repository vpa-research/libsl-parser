package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation

data class AnnotationReference (
    val name: String,
    val argTypes: List<TypeReference>,
    override val context: LslContextBase
) : LslReference<Annotation, AnnotationReference> {
    override fun resolve(): Annotation? {
        println(this.name)
        println(argTypes)
        return context.resolveAnnotation(this)
    }

    // todo: Improve a support for overloading
    override fun isSameReference(other: AnnotationReference): Boolean {
        return other.name == this.name && other.argTypes.size == this.argTypes.size
    }

    // todo: Improve a support for overloading
    override fun isReferenceMatchWithNode(node: Annotation): Boolean {
        return this.name == node.name && this.argTypes.size == node.argumentDescriptors.size
    }

    override fun toString(): String {
        return "AnnotationReference($name)"
    }
}

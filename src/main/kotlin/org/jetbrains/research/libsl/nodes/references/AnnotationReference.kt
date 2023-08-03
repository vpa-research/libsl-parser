package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.type.*

data class AnnotationReference(
    val name: String,
    val argTypes: List<TypeReference>,
    override val context: LslContextBase
) : LslReference<Annotation, AnnotationReference> {
    override fun resolve(): Annotation? {
        return context.resolveAnnotation(this)
    }

    override fun isSameReference(other: AnnotationReference): Boolean {
        if(other.name != this.name || other.argTypes.size != this.argTypes.size) {
            return false
        }
        return other.argTypes.zip(this.argTypes).all { (otherType, thisType) ->
            otherType.resolveOrError().fullName == thisType.resolveOrError().fullName
        }
    }

    override fun isReferenceMatchWithNode(node: Annotation): Boolean {
        if(this.name != node.name || this.argTypes.size != node.argumentDescriptors.size) {
            return false
        }
        return this.argTypes.zip(node.argumentDescriptors).all { (refType, nodeType) ->
            val resolvedRefType = checkIfTypeAliasAndResolve(refType)
            val refTypeName = resolvedRefType.name
            val checkedNodeTypeRef = checkIfTypeAliasAndResolve(nodeType.typeReference)
            val nodeTypeName = checkedNodeTypeRef.name
            val checkedTypeGeneric = checkedNodeTypeRef.generic
            val checkedRefTypeGeneric = resolvedRefType.generic?.let { checkIfTypeAliasAndResolve(it) }
            val resolvedNodeGeneric = checkedTypeGeneric?.let { checkIfTypeAliasAndResolve(it) }

            if(resolvedRefType !is ArrayType && checkedNodeTypeRef.name == AnyType.ANY_TYPE_NAME) {
                return true
            }

            if(refTypeName != nodeTypeName) {
                return false
            }

            if(resolvedRefType is ArrayType && checkedNodeTypeRef is ArrayType) {
                if (checkedRefTypeGeneric != null && resolvedNodeGeneric != null) {
                    if(checkedRefTypeGeneric.name == NothingType.Nothing_TYPE_NAME) {
                        return true
                    }
                    if(checkedRefTypeGeneric.name != resolvedNodeGeneric.name) {
                        return false
                    }
                }
            }

            if(checkedRefTypeGeneric != null && resolvedNodeGeneric != null) {
                if(checkedRefTypeGeneric.name != resolvedNodeGeneric.name) {
                    return false
                }
            }

            return true
        }
    }

    private fun checkIfTypeAliasAndResolve(t: TypeReference): Type {
        val resolvedT = t.resolveOrError()
        return if(resolvedT is TypeAlias) {
            resolvedT.originalType.resolveOrError()
        } else {
            resolvedT
        }
    }

    override fun toString(): String {
        return "AnnotationReference($name)"
    }
}

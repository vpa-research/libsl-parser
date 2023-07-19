package org.jetbrains.research.libsl.nodes.references

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.ActionDecl
import org.jetbrains.research.libsl.type.*

data class ActionDeclReference(
    val name: String,
    val paramTypes: List<TypeReference>,
    override val context: LslContextBase
) : LslReference<ActionDecl, ActionDeclReference> {
    override fun resolve(): ActionDecl? {
        return context.resolveActionDecl(this)
    }

    override fun isSameReference(other: ActionDeclReference): Boolean {
        if(other.name != this.name || other.paramTypes.size != this.paramTypes.size) {
            return false
        }
        return other.paramTypes.zip(this.paramTypes).all { (otherType, thisType) ->
            otherType.resolveOrError().fullName == thisType.resolveOrError().fullName
        }
    }

    override fun isReferenceMatchWithNode(node: ActionDecl): Boolean {
        if(this.name != node.name || this.paramTypes.size != node.argumentDescriptors.size) {
            return false
        }
        return this.paramTypes.zip(node.argumentDescriptors).all { (refType, nodeType) ->
            val refTypeName = getRealTypeName(refType)
            val nodeTypeName = getRealTypeName(nodeType.typeReference)
            val resolvedRefType = checkIfTypeAliasAndResolve(refType)
            val checkedNodeTypeRef = checkIfTypeAliasAndResolve(nodeType.typeReference)
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

    private fun getRealTypeName(refType: TypeReference): String {
        val s = when (val resolvedRefType = refType.resolveOrError()) {
            is TypeAlias ->
                resolvedRefType.originalType.resolveOrError().name

            else ->
                resolvedRefType.name
        }
        return s
    }

    override fun toString(): String {
        return "ActionReference($name)"
    }
}
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.ActionDecl
import org.jetbrains.research.libsl.nodes.references.ActionDeclReference
import org.jetbrains.research.libsl.nodes.references.TypeReference

fun build(
    name: String,
    paramTypes: List<TypeReference>,
    context: LslContextBase
): ActionDeclReference {
    return ActionDeclReference(name, paramTypes, context)
}

fun ActionDecl.getReference(context: LslContextBase): ActionDeclReference {
    return build(this.name, this.values.map { value -> value.typeReference }, context)
}
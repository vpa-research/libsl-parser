package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Annotation
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.references.*
import org.jetbrains.research.libsl.type.*

class LslGlobalContext : LslContextBase() {
    @Suppress("MemberVisibilityCanBePrivate")
    var isInitialized: Boolean = false

    private val importedContexts = mutableListOf<LslGlobalContext>()

    override val parentContext: LslContextBase? = null

    fun init() {
        if (isInitialized)
            return
        val types = buildList<Type> {
            for (pointer in listOf(true, false)) {
                add(Int8Type(this@LslGlobalContext, pointer))
                add(Int16Type(this@LslGlobalContext, pointer))
                add(Int32Type(this@LslGlobalContext, pointer))
                add(Int64Type(this@LslGlobalContext, pointer))

                add(UnsignedInt8Type(this@LslGlobalContext, pointer))
                add(UnsignedInt16Type(this@LslGlobalContext, pointer))
                add(UnsignedInt32Type(this@LslGlobalContext, pointer))
                add(UnsignedInt64Type(this@LslGlobalContext, pointer))


                add(Float32Type(this@LslGlobalContext, pointer))
                add(Float64Type(this@LslGlobalContext, pointer))

                add(BoolType(this@LslGlobalContext, pointer))
                add(CharType(this@LslGlobalContext, pointer))
                add(StringType(this@LslGlobalContext, pointer))
                add(VoidType(this@LslGlobalContext, pointer))

                add(AnyType(this@LslGlobalContext))
                add(NothingType(this@LslGlobalContext))
            }
        }

        types.forEach(::storeType)
        this.isInitialized = true
    }

    fun import(context: LslGlobalContext) {
        importedContexts.add(context)
    }

    override fun resolveVariable(reference: VariableReference): Variable? {
        return resolveVariable(reference, setOf(this))
    }

    override fun resolveType(reference: TypeReference): Type? {
        return resolveType(reference, setOf(this))
    }

    override fun resolveAutomaton(reference: AutomatonReference): Automaton? {
        return resolveAutomaton(reference, setOf(this))
    }

    override fun resolveFunction(reference: FunctionReference): Function? {
        return resolveFunction(reference, setOf(this))
    }

    override fun resolveAnnotation(reference: AnnotationReference): Annotation? {
        return resolveAnnotation(reference, setOf(this))
    }

    override fun resolveActionDecl(reference: ActionDeclReference): ActionDecl? {
        return resolveActionDecl(reference, setOf(this))
    }

    private fun resolveVariable(reference: VariableReference, visitedScopes: Set<LslGlobalContext>): Variable? {
        return super.resolveVariable(reference)
            ?: resolveInImportedContexts(visitedScopes) { v -> resolveVariable(reference, v) }
    }

    private fun resolveType(reference: TypeReference, visitedScopes: Set<LslGlobalContext>): Type? {
        return super.resolveType(reference)
            ?: resolveInImportedContexts(visitedScopes) { v -> resolveType(reference, v) }
    }

    private fun resolveAutomaton(reference: AutomatonReference, visitedScopes: Set<LslGlobalContext>): Automaton? {
        return super.resolveAutomaton(reference)
            ?: resolveInImportedContexts(visitedScopes) { v -> resolveAutomaton(reference, v) }
    }

    private fun resolveFunction(reference: FunctionReference, visitedScopes: Set<LslGlobalContext>): Function? {
        return super.resolveFunction(reference)
            ?: resolveInImportedContexts(visitedScopes) { v -> resolveFunction(reference, v) }
    }

    private fun resolveAnnotation(reference: AnnotationReference, visitedScopes: Set<LslGlobalContext>): Annotation? {
        return super.resolveAnnotation(reference)
            ?: resolveInImportedContexts(visitedScopes) {v -> resolveAnnotation(reference, v)}
    }

    private fun resolveActionDecl(reference: ActionDeclReference, visitedScopes: Set<LslGlobalContext>): ActionDecl? {
        return super.resolveActionDecl(reference)
            ?: resolveInImportedContexts(visitedScopes) { v -> resolveActionDecl(reference, v) }
    }

    private fun <T> resolveInImportedContexts(
        visitedScopes: Set<LslGlobalContext>,
        resolve: LslGlobalContext.(Set<LslGlobalContext>) -> T
    ): T? {
        return importedContexts
            .filter { it !in visitedScopes }
            .firstNotNullOfOrNull { ctx -> ctx.resolve(visitedScopes + ctx) }
    }

    override fun hashCode(): Int {
        // DO NOT USE HERE ANY NODE because it could contain a reference to context consequently a SOF will occur
        return 42
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

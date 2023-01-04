package org.jetbrains.research.libsl.context

import org.jetbrains.research.libsl.nodes.Automaton
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.nodes.Variable
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.FunctionReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.nodes.references.VariableReference
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
                add(IntType(this@LslGlobalContext, IntType.IntCapacity.INT8, pointer))
                add(IntType(this@LslGlobalContext, IntType.IntCapacity.INT16, pointer))
                add(IntType(this@LslGlobalContext, IntType.IntCapacity.INT32, pointer))
                add(IntType(this@LslGlobalContext, IntType.IntCapacity.INT64, pointer))

                add(UnsignedType(this@LslGlobalContext, UnsignedType.UnsignedCapacity.UNSIGNED8, pointer))
                add(UnsignedType(this@LslGlobalContext, UnsignedType.UnsignedCapacity.UNSIGNED16, pointer))
                add(UnsignedType(this@LslGlobalContext, UnsignedType.UnsignedCapacity.UNSIGNED32, pointer))
                add(UnsignedType(this@LslGlobalContext, UnsignedType.UnsignedCapacity.UNSIGNED64, pointer))

                add(FloatType(this@LslGlobalContext, FloatType.FloatCapacity.FLOAT32, pointer))
                add(FloatType(this@LslGlobalContext, FloatType.FloatCapacity.FLOAT64, pointer))

                add(BoolType(this@LslGlobalContext, pointer))
                add(CharType(this@LslGlobalContext, pointer))
                add(StringType(this@LslGlobalContext, pointer))
                add(VoidType(this@LslGlobalContext, pointer))
            }
        }

        types.forEach(::storeType)
        this.isInitialized = true
    }

    fun import(context: LslGlobalContext) {
        importedContexts.add(context)
    }

    override fun resolveVariable(reference: VariableReference): Variable? {
        return super.resolveVariable(reference) ?: resolveInImportedContexts { resolveVariable(reference) }
    }

    override fun resolveType(reference: TypeReference): Type? {
        return super.resolveType(reference) ?: resolveInImportedContexts { resolveType(reference) }
    }

    override fun resolveAutomaton(reference: AutomatonReference): Automaton? {
        return super.resolveAutomaton(reference) ?: resolveInImportedContexts { resolveAutomaton(reference) }
    }

    override fun resolveFunction(reference: FunctionReference): Function? {
        return super.resolveFunction(reference) ?: resolveInImportedContexts { resolveFunction(reference) }
    }

    private fun <T> resolveInImportedContexts(
        resolve: LslGlobalContext.() -> T
    ): T? {
        return importedContexts.firstNotNullOfOrNull { ctx -> ctx.resolve() }
    }

    override fun hashCode(): Int {
        // DO NOT USE HERE ANY NODE because it could contain a reference to context consequently a SOF will occur
        return 42
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LslGlobalContext) return false

        if (isInitialized != other.isInitialized) return false
        if (importedContexts != other.importedContexts) return false
        if (parentContext != other.parentContext) return false

        return true
    }
}

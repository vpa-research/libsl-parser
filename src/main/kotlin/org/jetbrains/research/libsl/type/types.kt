package org.jetbrains.research.libsl.type

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.Atomic
import org.jetbrains.research.libsl.nodes.IPrinter
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed interface Type : IPrinter {
    val name: String
    val isPointer: Boolean
    val context: LslContextBase
    val generic: TypeReference?

    val fullName: String
        get() = buildString {
            append(if (isPointer) "*" else "")
            append(name)
            append(if (generic != null) "<${generic!!.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL}>" else "")
        }

    val isArray: Boolean
        get() = (this as? TypeAlias)?.originalType?.resolve()?.isArray == true || this is ArrayType

    val isTopLevelType: Boolean
        get() = false

    val isTypeBlockType: Boolean
        get() = false

    companion object {
        const val UNRESOLVED_TYPE_SYMBOL = "`<UNRESOLVED_TYPE>`"
    }
}

sealed interface LibslType : Type
data class RealType(
    val nameParts: List<String>,
    override val isPointer: Boolean = false,
    override val generic: TypeReference? = null,
    override val context: LslContextBase
) : Type {
    override val name: String
        get() = nameParts.joinToString(".")

    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = fullName
}

data class SimpleType(
    override val name: String,
    val realType: Type,
    override val isPointer: Boolean = false,
    override val context: LslContextBase
) : LibslType {
    override val generic: TypeReference? = null
    override val isTypeBlockType: Boolean = true

    override fun dumpToString(): String {
        return "${BackticksPolitics.forTypeIdentifier(name)}(${BackticksPolitics.forTypeIdentifier(realType.fullName)});"
    }

    override fun toString() = dumpToString()
}

data class TypeAlias(
    override val name: String,
    val originalType: TypeReference,
    override val context: LslContextBase
) : LibslType {
    override val isPointer: Boolean = false
    override val generic: TypeReference? = null

    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String {
        return buildString {
            append("typealias ")
            append(BackticksPolitics.forTypeIdentifier(name))
            append(" = ")
            append(BackticksPolitics.forTypeIdentifier(originalType.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL))
            append(";")
        }
    }

    override fun toString() = dumpToString()
}

data class EnumLikeSemanticType(
    override val name: String,
    val type: Type,
    val entries: Map<String, Atomic>,
    override val context: LslContextBase
) : LibslType {
    override val isPointer: Boolean = false
    override val generic: TypeReference? = null
    override val isTypeBlockType: Boolean = true

    override fun dumpToString(): String = buildString {
        appendLine("$name(${type.fullName}) {")
        val formattedEntries = entries.map { (k, v) -> "${BackticksPolitics.forIdentifier(k)}: ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }

    override fun toString() = dumpToString()
}

data class StructuredType(
    override val name: String,
    var entries: Map<String, TypeReference>,
    override val context: LslContextBase
) : Type {
    override val isPointer: Boolean = false
    override val isTopLevelType: Boolean = true
    override val generic: TypeReference? = null

    override fun dumpToString(): String = buildString {
        appendLine("type ${name} {")
        val formattedEntries = entries.map { (k, v) ->
            "${BackticksPolitics.forIdentifier(k)}: ${BackticksPolitics.forTypeIdentifier(v.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL)}"
        }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructuredType) return false

        if (name != other.name) return false
        if (generic != other.generic) return false
        if (entries != other.entries) return false
        if (context != other.context) return false
        if (isPointer != other.isPointer) return false
        if (isTopLevelType != other.isTopLevelType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + isPointer.hashCode()
        result = 31 * result + isTopLevelType.hashCode()
        return result
    }

    override fun toString() = dumpToString()
}

data class EnumType(
    override val name: String,
    val entries: Map<String, Atomic>,
    override val context: LslContextBase
) : Type {
    override val isPointer: Boolean = false
    override val generic: TypeReference? = null
    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String = buildString {
        appendLine("enum $name {")
        val formattedEntries = entries.map { (k, v) -> "${BackticksPolitics.forIdentifier(k)} = ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }

    override fun toString() = dumpToString()
}

data class ArrayType(
    override val isPointer: Boolean = false,
    override val generic: TypeReference,
    override val context: LslContextBase
) : Type {
    override val name: String = "array"

    override fun dumpToString(): String {
        return BackticksPolitics.forTypeIdentifier(fullName)
    }

    override fun toString() = dumpToString()
}

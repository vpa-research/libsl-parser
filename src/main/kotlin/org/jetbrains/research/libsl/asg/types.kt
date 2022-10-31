package org.jetbrains.research.libsl.asg

sealed interface Type : IPrinter {
    val name: String
    val isPointer: Boolean
    val context: LslContext
    val generic: Type?

    val fullName: String
        get() = "${if (isPointer) "*" else ""}$name${if (generic != null) "<${generic!!.fullName}>" else ""}"

    val isArray: Boolean
        get() = (this as? TypeAlias)?.originalType?.isArray == true || this is ArrayType

    val isTopLevelType: Boolean
        get() = false

    val isTypeBlockType: Boolean
        get() = false
}

sealed interface LibslType : Type
data class RealType (
    val nameParts: List<String>,
    override val isPointer: Boolean = false,
    override val generic: Type? = null,
    override val context: LslContext
) : Type {
    override val name: String
        get() = nameParts.joinToString(".")

    override fun toString(): String = "${if (isPointer) "*" else ""}$name${if (generic != null) "<${generic.fullName}>" else ""}"

    override fun dumpToString(): String {
        return toString()
    }
}

data class SimpleType(
    override val name: String,
    val realType: Type,
    override val isPointer: Boolean = false,
    override val context: LslContext
) : LibslType {
    override val generic: Type? = null
    override val isTypeBlockType: Boolean = true

    override fun dumpToString(): String {
        return "$name(${realType.dumpToString()});"
    }
}

sealed interface AliassableType : LibslType
data class TypeAlias (
    override val name: String,
    val originalType: AliassableType,
    override val context: LslContext
) : LibslType {
    override val isPointer: Boolean = false
    override val generic: Type? = null

    override val isTopLevelType: Boolean = true

    override fun dumpToString(): String {
        return "typealias $name = ${originalType.fullName};"
    }
}

data class EnumLikeSemanticType(
    override val name: String,
    val type: Type,
    val entries: Map<String, Atomic>,
    override val context: LslContext
) : LibslType, FieldValuedType, FieldTypedType {
    override val isPointer: Boolean = false
    override val generic: Type? = null
    override val isTypeBlockType: Boolean = true

    override fun getFieldValue(name: String): Expression? {
        return entries[name]
    }

    override fun getFieldType(name: String): Type? {
        if (entries.isEmpty())
            return null

        val valueTypes = entries.values.map { context.typeInferer.getExpressionType(it) }
        return valueTypes.drop(1).fold(valueTypes.first()) { acc, next ->
            context.typeInferer.mergeTypesOrNull(acc, next) ?: return null
        }
    }

    override fun dumpToString(): String = buildString {
        appendLine("$name(${type.fullName}) {")
        val formattedEntries = entries.map { (k, v) -> "$k: ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }
}

class ChildrenType(
    override val name: String,
    override val context: LslContext,
) : Type {
    override val generic: Type? = null
    override val isPointer: Boolean = false

    override fun dumpToString(): String {
        error("unsupported operation exception")
    }
}

data class StructuredType(
    override val name: String,
    val type: Type,
    override val generic: Type? = null,
    var entries: Map<String, Type>,
    override val context: LslContext
) : AliassableType, FieldTypedType {
    override val isPointer: Boolean = false
    override val isTopLevelType: Boolean = true

    override fun getFieldType(name: String): Type? {
        return entries[name]
    }

    override fun dumpToString(): String = buildString {
        appendLine("type ${type.fullName} {")
        val formattedEntries = entries.map { (k, v) -> "$k: ${v.fullName}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }

    override fun toString(): String {
        return "StructuredType(name='$name', type=$type, generic=$generic, isPointer=$isPointer, isTopLevelType=$isTopLevelType)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructuredType) return false

        if (name != other.name) return false
        if (type != other.type) return false
        if (generic != other.generic) return false
        if (entries != other.entries) return false
        if (context != other.context) return false
        if (isPointer != other.isPointer) return false
        if (isTopLevelType != other.isTopLevelType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (generic?.hashCode() ?: 0)
        result = 31 * result + entries.hashCode()
        result = 31 * result + context.hashCode()
        result = 31 * result + isPointer.hashCode()
        result = 31 * result + isTopLevelType.hashCode()
        return result
    }
}

data class EnumType(
    override val name: String,
    val entries: Map<String, Atomic>,
    override val context: LslContext
) : AliassableType, FieldValuedType, FieldTypedType {
    override val isPointer: Boolean = false
    override val generic: Type? = null
    override val isTopLevelType: Boolean = true

    override fun getFieldValue(name: String): Expression? {
        return entries[name]
    }

    override fun getFieldType(name: String): Type? {
        if (entries.isEmpty())
            return null

        val valueTypes = entries.values.map { context.typeInferer.getExpressionType(it) }
        return valueTypes.drop(1).fold(valueTypes.first()) { acc, next ->
            context.typeInferer.mergeTypesOrNull(acc, next) ?: return null
        }
    }

    override fun dumpToString(): String = buildString {
        appendLine("enum $name {")
        val formattedEntries = entries.map { (k, v) -> "$k = ${v.dumpToString()}" }
        append(withIndent(simpleCollectionFormatter(formattedEntries, "", ";", addEmptyLastLine = false)))
        append("}")
    }
}

data class ArrayType(
    override val name: String,
    override val isPointer: Boolean = false,
    override val generic: Type,
    override val context: LslContext
) : AliassableType {
    override fun dumpToString(): String {
        return fullName
    }
}

sealed interface FieldTypedType {
    fun getFieldType(name: String): Type?
}

sealed interface FieldValuedType {
    fun getFieldValue(name: String): Expression?
}

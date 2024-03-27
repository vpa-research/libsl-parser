package org.jetbrains.research.libsl.nodes


enum class GenericTypeKind(val string: String) {
    IN("in"), OUT("out"), PLAIN("plain");

    companion object {
        fun fromString(str: String) = values().first { op -> op.string == str }
    }
}

open class Generic(
    open var name: String,
    open var type: GenericTypeKind
) {


    override fun toString(): String {
        return name;
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Generic

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}
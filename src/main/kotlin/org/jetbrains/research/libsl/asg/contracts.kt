package org.jetbrains.research.libsl.asg

data class Contract(
    val name: String?,
    val expression: Expression,
    val kind: ContractKind
) : Node() {
    override fun dumpToString(): String = buildString {
        append(kind.keyword)
        append(IPrinter.SPACE)
        if (name != null) {
            append(name)
            append(": ")
        }
        append(expression.dumpToString())
        append(";")
    }
}

enum class ContractKind(val keyword: String) {
    REQUIRES("requires"), ENSURES("ensures"), ASSIGNS("assigns")
}
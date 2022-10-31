package org.jetbrains.research.libsl.asg

open class Annotation(
    val name: String,
    val values: MutableList<Expression> = mutableListOf()
) : IPrinter {
    override fun toString(): String {
        return "Annotation(name='$name', values=$values)"
    }

    override fun dumpToString(): String = buildString {
        append("@$name")
        if (values.isNotEmpty()) {
            append(values.joinToString(prefix = "(", postfix = ")", separator = ", ") { v -> v.dumpToString() })
        }

        appendLine()
    }
}

class TargetAnnotation(
    name: String,
    values: MutableList<Expression>,
    val targetAutomaton: Automaton
) : Annotation(name, values) {
    override fun toString(): String {
        return "TargetAnnotation(name='$name', values=$values, target=$targetAutomaton)"
    }
}
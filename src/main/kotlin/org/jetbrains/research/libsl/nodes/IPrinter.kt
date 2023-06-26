package org.jetbrains.research.libsl.nodes

interface IPrinter {
    fun dumpToString(): String

    fun formatListEmptyLineAtEndIfNeeded(
        printers: List<IPrinter>,
        onSeparatedLines: Boolean = true,
        appendEndLineAtTheEnd: Boolean = printers.isNotEmpty() && onSeparatedLines
    ): String = buildString {
        for (printer in printers.dropLast(1)) {
            append(printer.dumpToString())
            if (onSeparatedLines) {
                appendLine()
            } else {
                append(SPACE)
            }
        }
        if (printers.isNotEmpty()) {
            append(printers.last().dumpToString())
        }
        if (appendEndLineAtTheEnd) {
            appendLine()
        }
    }

    fun simpleCollectionFormatter(
        collection: Collection<String>,
        prefix: String = "",
        suffix: String = "",
        addEmptyLastLine: Boolean = false
    ): String = buildString {
        for (element in collection) {
            appendLine("$prefix$element$suffix")
        }

        if (addEmptyLastLine && collection.isNotEmpty()) {
            appendLine()
        }
    }

    fun withIndent(text: String) = buildString {
        currentIndentLevel++

        val lines = text.lines()
        for (line in lines.dropLast(1)) {
            appendLine(getLineWithIndent(line))
        }
        lines.lastOrNull()?.let { line ->
            if (line.isNotEmpty()) {
                append(getLineWithIndent(line))
            }
        }

        currentIndentLevel--
    }

    private fun getLineWithIndent(line: String): String {
        return SPACE.repeat(currentIndent) + line
    }

    companion object {
        const val SPACE = " "

        var currentIndentLevel = 0
        const val indentLevelSize = 4

        val currentIndent: Int
            get() = currentIndentLevel * indentLevelSize
    }
}

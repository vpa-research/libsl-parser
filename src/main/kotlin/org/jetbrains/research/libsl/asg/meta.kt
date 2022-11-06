package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.visitors.addBacktickIfNeeded

data class Library(
    val metadata: MetaNode,
    val imports: MutableList<String> = mutableListOf(),
    val includes: MutableList<String> = mutableListOf(),
    val semanticTypes: MutableList<Type> = mutableListOf(),
    val automata: MutableList<Automaton> = mutableListOf(),
    val extensionFunctions: MutableMap<String, MutableList<Function>> = mutableMapOf(),
    val globalVariableDeclarations: MutableMap<String, GlobalVariableDeclaration> = mutableMapOf()
) : Node() {
    override fun dumpToString(): String = buildString {
        appendLine(metadata.dumpToString())
        append(formatImports())
        append(formatIncludes())
        append(formatTopLevelSemanticTypes())
        append(formatSemanticTypeBlock())
        append(formatGlobalVariables())
        append(formatAutomata())
    }

    private fun formatImports(): String {
        return simpleCollectionFormatter(imports, prefix = "import${IPrinter.SPACE}", suffix = ";", addEmptyLastLine = true)
    }

    private fun formatIncludes(): String {
        return simpleCollectionFormatter(includes, prefix = "include${IPrinter.SPACE}", suffix = ";", addEmptyLastLine = true)
    }

    private fun formatTopLevelSemanticTypes(): String {
        val topLevelTypes = semanticTypes.filter { type -> type.isTopLevelType }
        val formattedTypes = topLevelTypes.map { type -> type.dumpToString() }

        return simpleCollectionFormatter(collection = formattedTypes, suffix = "\n")
    }

    private fun formatSemanticTypeBlock(): String = buildString {
        val types = semanticTypes.filter { type -> type.isTypeBlockType }
        if (types.isEmpty())
            return@buildString

        appendLine("types {")
        append(withIndent(formatListEmptyLineAtEndIfNeeded(types)))
        appendLine("}")
    }

    private fun formatAutomata(): String = buildString {
        if (automata.isEmpty())
            return@buildString

        append(formatListEmptyLineAtEndIfNeeded(automata))
    }

    private fun formatGlobalVariables(): String = formatListEmptyLineAtEndIfNeeded(globalVariableDeclarations.values.toList())
}

class MetaNode(
    var name: String,
    val libraryVersion: String? = null,
    val language: String? = null,
    var url: String? = null,
    val lslVersion: Triple<UInt, UInt, UInt>
) : Node() {
    val stringVersion: String
        get() {
            return "${lslVersion.first}.${lslVersion.second}.${lslVersion.third}"
        }

    // libsl "$libslVersion";
    // library $libraryName version "$libraryVersion" language "$language" url "libraryUrl"
    override fun dumpToString(): String = buildString {
        appendLine("libsl \"$stringVersion\";")
        append("library ${addBacktickIfNeeded(name)}")

        if (libraryVersion != null) {
            append(IPrinter.SPACE + "version \"$libraryVersion\"")
        }

        if (language != null) {
            append(IPrinter.SPACE + "language \"$language\"")
        }

        if (url != null) {
            append(IPrinter.SPACE + "url \"$url\"")
        }
        appendLine(";")
    }
}
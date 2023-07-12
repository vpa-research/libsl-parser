package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.*
import org.jetbrains.research.libsl.type.Type
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class Library(
    val fileName: String,
    val metadata: MetaNode?,
    val importNames: MutableList<String> = mutableListOf(),
    val importsMap: MutableMap<String, Library> = mutableMapOf(),
    val includes: MutableList<String> = mutableListOf(),
    val semanticTypesReferences: MutableList<TypeReference> = mutableListOf(),
    val automataReferences: MutableList<AutomatonReference> = mutableListOf(),
    val extensionFunctionsReferences: MutableList<FunctionReference> = mutableListOf(),
    val globalVariableReferences: MutableList<VariableReference> = mutableListOf(),
    val annotationReferences: MutableList<AnnotationReference> = mutableListOf(),
    val actionReferences: MutableList<ActionReference> = mutableListOf(),
) : Node() {
    val resolvedTypes: List<Type>
        get() = semanticTypesReferences.map { it.resolveOrError() }
    val automata: List<Automaton>
        get() = automataReferences.map { it.resolveOrError() }
    private val annotations: List<Annotation>
        get() = annotationReferences.map { it.resolveOrError() }
    private val globalVariables: List<Variable>
        get() = globalVariableReferences.map { it.resolveOrError() }
    private val actions: List<Action>
        get() = actionReferences.map { it.resolveOrError() }
    val importedAutomataMap: Map<String, List<Automaton>>
        get() = importsMap.mapValues { (_, library) -> library.automata }
    val importedSemanticTypesMap: Map<String, List<Type>>
        get() = importsMap.mapValues { (_, library) -> library.resolvedTypes }


    override fun dumpToString(): String = buildString {
        if(metadata != null) {
            appendLine(metadata.dumpToString())
        }
        append(formatImports())
        append(formatIncludes())
        append(formatTopLevelSemanticTypes())
        append(formatSemanticTypeBlock())
        append(formatGlobalVariables())
        append(formatDeclaredAnnotations())
        append(formatActionDeclarations())
        append(formatAutomata())
        appendLine()
    }

    private fun formatImports(): String {
        return simpleCollectionFormatter(
            importNames,
            prefix = "import${IPrinter.SPACE}",
            suffix = ";",
            addEmptyLastLine = true
        )
    }

    private fun formatIncludes(): String {
        return simpleCollectionFormatter(
            includes,
            prefix = "include${IPrinter.SPACE}",
            suffix = ";",
            addEmptyLastLine = true
        )
    }

    private fun formatTopLevelSemanticTypes(): String {
        val topLevelTypes = resolvedTypes.filter { type ->
            type.isTopLevelType
        }
        val formattedTypes = topLevelTypes.map { type -> type.dumpToString() }

        return simpleCollectionFormatter(collection = formattedTypes, suffix = "\n")
    }

    private fun formatSemanticTypeBlock(): String = buildString {
        val types = resolvedTypes.filter { type ->
            type.isTypeBlockType
        }

        if (types.isEmpty())
            return@buildString

        appendLine("types {")
        append(withIndent(formatListEmptyLineAtEndIfNeeded(types)))
        appendLine("}")
    }

    private fun formatDeclaredAnnotations(): String = buildString {
        annotations.forEach { annotation ->
            append(annotation.dumpToString())
        }
    }

    private fun formatActionDeclarations(): String = buildString {
        actions.forEach { action ->
            append(action.dumpToString())
        }
    }

    private fun formatAutomata(): String = formatListEmptyLineAtEndIfNeeded(automata)

    private fun formatGlobalVariables(): String = formatListEmptyLineAtEndIfNeeded(globalVariables)
}

class MetaNode(
    val lslVersion: LslVersion,
    var name: String,
    val libraryVersion: String? = null,
    val language: String? = null,
    var url: String? = null
) : Node() {
    val stringVersion: String
        get() {
            return lslVersion.dumpToString()
        }

    // libsl "$libslVersion";
    // library $libraryName version "$libraryVersion" language "$language" url "libraryUrl"
    override fun dumpToString(): String = buildString {
        appendLine("libsl \"$stringVersion\";")
        append("library ${BackticksPolitics.forIdentifier(name)}")

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

data class LslVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : IPrinter {
    override fun dumpToString(): String {
        return "$major.$minor.$patch"
    }

    companion object {
        fun fromString(str: String): LslVersion {
            val parts = str.split(".").mapNotNull { part -> part.toIntOrNull() }
            check(parts.size == 3) { "Unknown LibSL version format: $str" }

            val major = parts[0]
            val minor = parts[1]
            val patch = parts[2]

            return LslVersion(major, minor, patch)
        }
    }
}

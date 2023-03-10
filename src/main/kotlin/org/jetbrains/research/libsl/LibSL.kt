package org.jetbrains.research.libsl

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.jetbrains.annotations.TestOnly
import org.jetbrains.research.libsl.LibSLParser.FileContext
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.visitors.LibrarySpecificationVisitor
import org.jetbrains.research.libsl.visitors.TopLevelDeclarationsResolver
import org.jetbrains.research.libsl.visitors.TypeResolver
import java.io.File
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate", "unused")
class LibSL(
    private val basePath: String,
    val context: LslGlobalContext = LslGlobalContext()
) {
    val errorManager = ErrorManager()
    lateinit var library: Library
    private var isParsed = false
    private val processedFiles = mutableSetOf<String>()

    init {
        context.init()
    }

    var errorListener: BaseErrorListener? = null

    fun loadFromFile(file: File): Library {
        processedFiles.add(file.nameWithoutExtension)
        return loadFromString(file.readText())
    }

    fun loadByPath(path: Path): Library {
        return loadFromFile(path.toFile())
    }

    fun loadFromPath(path: String): Library {
        return loadFromFile(File(path))
    }

    fun loadFromFileName(name: String): Library {
        return loadByPath(Path.of(basePath).resolve(name))
    }

    fun loadFromString(string: String): Library {
        val stream = CharStreams.fromString(string)
        val lexer = LibSLLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = LibSLParser(tokenStream)

        if (errorListener != null) {
            parser.addErrorListener(errorListener)
        }

        val file = parser.file()
        val library = processFileRule(file)

        for (importName in library.imports) {
            if (importName in processedFiles)
                continue

            loadFromFileName("$importName.lsl")
        }

        return library
    }

    private fun processFileRule(file: FileContext): Library {
        val librarySpecificationVisitor = LibrarySpecificationVisitor(basePath, errorManager, context)
        return librarySpecificationVisitor.processFile(file)
    }
}

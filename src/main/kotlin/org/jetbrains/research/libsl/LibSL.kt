package org.jetbrains.research.libsl

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTreeListener
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

    var errorListener: ParseTreeListener? = null
    
    fun loadFromFile(file: File): Library {
        return loadFromString(file.readText())
    }

    fun loadByPath(path: Path): Library {
        return loadFromFile(path.toFile())
    }

    fun loadFromPath(path: String): Library {
        return loadFromFile(File(path))
    }

    fun loadFromString(string: String): Library {
        val stream = CharStreams.fromString(string)
        val lexer = LibSLLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        context.init()
        val parser = LibSLParser(tokenStream)
        if (errorListener != null) {
            parser.addParseListener(errorListener)
        }

        val file = parser.file()
        return processFileRule(file)
    }

    private fun processFileRule(file: FileContext): Library {
        val librarySpecificationVisitor = LibrarySpecificationVisitor(basePath, errorManager, context)
        return librarySpecificationVisitor.processFile(file)
    }
}

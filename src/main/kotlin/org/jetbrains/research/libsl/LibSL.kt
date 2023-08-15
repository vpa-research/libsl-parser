package org.jetbrains.research.libsl

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jetbrains.research.libsl.LibSLParser.FileContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.UnresolvedImportOrInclude
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl.nodes.LslVersion
import org.jetbrains.research.libsl.nodes.MetaNode
import org.jetbrains.research.libsl.utils.EntityPosition
import org.jetbrains.research.libsl.utils.PositionGetter
import org.jetbrains.research.libsl.visitors.*
import java.io.File
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate", "unused")
class LibSL(
    private val basePath: String,
    val context: LslGlobalContext = LslGlobalContext(File(basePath).name)
) {
    val errorManager = ErrorManager()
    private var isParsed = false
    private val processedFiles = mutableSetOf<String>()
    private val thisFilesCanBeIgnored = mutableSetOf<String>()

    init {
        context.init()
    }

    var errorListener: BaseErrorListener? = null

    fun loadFromFile(file: File): Library {
        processedFiles.add(file.nameWithoutExtension)
        return loadFromString(file.readText(), file.name)
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

    fun loadFromString(string: String, fileName: String): Library {
        val stream = CharStreams.fromString(string)
        val lexer = LibSLLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = LibSLParser(tokenStream)

        if (errorListener != null) {
            parser.addErrorListener(errorListener)
        }

        val file = parser.file()

        val invokedLibrary = invokeLibrary(file, fileName)

        file.globalStatement().forEach { s ->
            if (s.ImportStatement() != null) {
                processImport(s.ImportStatement().text, invokedLibrary, PositionGetter().getCtxPosition(fileName, s))
            }
        }


        for (importName in invokedLibrary.importNames) {
            if (importName in thisFilesCanBeIgnored)
                continue
            thisFilesCanBeIgnored.add(importName)
            invokedLibrary.importsMap["$importName.lsl"] = loadFromFileName("$importName.lsl")
        }


        return processFileRule(file, fileName, invokedLibrary)
    }

    private fun processFileRule(file: FileContext, fileName: String, invokedLibrary: Library): Library {
        val librarySpecificationVisitor = LibrarySpecificationVisitor(fileName, invokedLibrary, basePath, errorManager, context)
        thisFilesCanBeIgnored.add(fileName.replace(".lsl",""))
        return librarySpecificationVisitor.processFile(file, invokedLibrary)
    }

    private fun invokeLibrary(file: FileContext, fileName: String): Library {

        val header = if(file.header() != null) {
            processHeader(file.header())
        } else {
            null
        }

        return Library(fileName, header)
    }

    private fun processHeader(ctx: LibSLParser.HeaderContext): MetaNode {
        val libraryName = ctx.libraryName.text.extractIdentifier()
        val libraryVersion = ctx.ver?.text?.removeDoubleQuotes()
        val libraryLanguage = ctx.lang?.text?.removeDoubleQuotes()
        val libraryUrl = ctx.link?.text?.removeDoubleQuotes()

        val libslVersionString = ctx.lslver.text.removeDoubleQuotes()
        val libslVersion = LslVersion.fromString(libslVersionString)

        return MetaNode(
            libslVersion,
            libraryName,
            libraryVersion,
            libraryLanguage,
            libraryUrl
        )
    }

    private fun processImport(str: String, invokedLibrary: Library, entityPosition: EntityPosition) {
        val importRegex = Regex("^(import)\\s+(.+);")
        val importName = importRegex.find(str)?.groupValues?.get(2)

        if (importName == null) {
            errorManager(UnresolvedImportOrInclude(str, entityPosition))
            return
        }

        invokedLibrary.importNames.add(importName)
    }
}

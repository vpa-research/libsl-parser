package org.jetbrains.research.libsl.visitors

import getReference
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.FileContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.Position
import org.jetbrains.research.libsl.errors.UnresolvedImportOrInclude
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl.nodes.LslVersion
import org.jetbrains.research.libsl.nodes.MetaNode
import org.jetbrains.research.libsl.nodes.references.builders.AnnotationReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.FunctionReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.VariableReferenceBuilder.getReference
import org.jetbrains.research.libsl.type.RealType

class LibrarySpecificationVisitor(
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val globalContext: LslGlobalContext
) : LibSLParserVisitor<Unit>(globalContext) {
    private lateinit var library: Library

    fun processFile(file: FileContext): Library {
        val header = processHeader(file.header())

        TypeResolver(basePath, errorManager, globalContext).visitFile(file)
        TopLevelDeclarationsResolver(basePath, errorManager, globalContext).visitFile(file)

        library = Library(
            header
        )

        file.globalStatement().forEach { visitGlobalStatement(it) }

        representTypesFromContextInLibrary()
        representAutomataFromContextInLibrary()
        representExtensionFunctionsFromContextInLibrary()
        representVariablesFromContextInLibrary()
        representDeclaredAnnotationsFromContextInLibrary()
        representDeclaredActionsFromContextInLibrary()

        return library
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

    override fun visitGlobalStatement(ctx: LibSLParser.GlobalStatementContext) {
        when {
            ctx.ImportStatement() != null -> processImport(ctx.ImportStatement().text, ctx.position())
            ctx.IncludeStatement() != null -> processInclude(ctx.IncludeStatement().text, ctx.position())
        }

        super.visitGlobalStatement(ctx)
    }

    private fun processImport(str: String, position: Position) {
        val importRegex = Regex("^(import)\\s+(.+);")
        val importName = importRegex.find(str)?.groupValues?.get(2)

        if (importName == null) {
            errorManager(UnresolvedImportOrInclude(str, position))
            return
        }

        library.imports.add(importName)

    }

    private fun processInclude(str: String, position: Position) {
        val includeRegex = Regex("^(include)\\s+(.+);")
        val includeName = includeRegex.find(str)?.groupValues?.get(2)

        if (includeName == null) {
            errorManager(UnresolvedImportOrInclude(str, position))
            return
        }

        library.includes.add(includeName)
    }

    private fun representTypesFromContextInLibrary() {
        val types = globalContext.getAllTypes()
        library.semanticTypesReferences.addAll(
            types.filter { it !is RealType }.map { type -> type.getReference(context) }
        )
    }

    private fun representAutomataFromContextInLibrary() {
        val automata = globalContext.getAllAutomata()
        library.automataReferences.addAll(automata.map { automaton -> automaton.getReference(context) })
    }

    private fun representExtensionFunctionsFromContextInLibrary() {
        val functions = globalContext.getAllFunctions()
        library.extensionFunctionsReferences.addAll(functions.map { func -> func.getReference(context) })
    }

    private fun representVariablesFromContextInLibrary() {
        val variables = globalContext.getAllVariables()
        library.globalVariableReferences.addAll(variables.map { variable -> variable.getReference(context) })
    }

    private fun representDeclaredAnnotationsFromContextInLibrary() {
        val annotations = globalContext.getAllAnnotations()
        library.annotationReferences.addAll(annotations.map { annotation -> annotation.getReference(context)})
    }

    private fun representDeclaredActionsFromContextInLibrary() {
        val declaredActions = globalContext.getAllDeclaredActions()
        library.declaredActionReferences.addAll(declaredActions.map { action -> action.getReference(context)})
    }
}

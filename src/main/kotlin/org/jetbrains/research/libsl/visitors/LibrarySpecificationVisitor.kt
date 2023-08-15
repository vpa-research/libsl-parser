package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.FileContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.errors.Position
import org.jetbrains.research.libsl.errors.UnresolvedImportOrInclude
import org.jetbrains.research.libsl.nodes.Library
import org.jetbrains.research.libsl.nodes.LslVersion
import org.jetbrains.research.libsl.nodes.MetaNode
import org.jetbrains.research.libsl.nodes.references.builders.ActionDeclReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.AnnotationReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.FunctionReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
import org.jetbrains.research.libsl.nodes.references.builders.VariableReferenceBuilder.getReference
import org.jetbrains.research.libsl.type.RealType
import org.jetbrains.research.libsl.utils.EntityPosition
import org.jetbrains.research.libsl.utils.PositionGetter

class LibrarySpecificationVisitor(
    val fileName: String,
    private val library: Library,
    private val basePath: String,
    private val errorManager: ErrorManager,
    private val globalContext: LslGlobalContext
) : LibSLParserVisitor<Unit>(globalContext) {
    private val posGetter = PositionGetter()

    fun processFile(file: FileContext, library: Library): Library {

        TypeResolver(basePath, errorManager, globalContext).visitFile(file)
        TopLevelDeclarationsResolver(basePath, errorManager, globalContext).visitFile(file)

        file.globalStatement().forEach { visitGlobalStatement(it) }

        representTypesFromContextInLibrary()
        representAutomataFromContextInLibrary()
        representExtensionFunctionsFromContextInLibrary()
        representVariablesFromContextInLibrary()
        representDeclaredAnnotationsFromContextInLibrary()
        representDeclaredActionsFromContextInLibrary()

        return library
    }

    override fun visitGlobalStatement(ctx: LibSLParser.GlobalStatementContext) {
        when {
            ctx.ImportStatement() != null -> processImport(ctx.ImportStatement().text, posGetter.getCtxPosition(fileName, ctx))
            ctx.IncludeStatement() != null -> processInclude(ctx.IncludeStatement().text, posGetter.getCtxPosition(fileName, ctx))
        }

        super.visitGlobalStatement(ctx)
    }

    private fun processImport(str: String, entityPosition: EntityPosition) {
        val importRegex = Regex("^(import)\\s+(.+);")
        val importName = importRegex.find(str)?.groupValues?.get(2)

        if (importName == null) {
            errorManager(UnresolvedImportOrInclude(str, entityPosition))
            return
        }
        if(importName !in library.importNames) {
            library.importNames.add(importName)
        }
    }

    private fun processInclude(str: String, entityPosition: EntityPosition) {
        val includeRegex = Regex("^(include)\\s+(.+);")
        val includeName = includeRegex.find(str)?.groupValues?.get(2)

        if (includeName == null) {
            errorManager(UnresolvedImportOrInclude(str, entityPosition))
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
        library.annotationReferences.addAll(annotations.map { annotation -> annotation.getReference(context) })
    }

    private fun representDeclaredActionsFromContextInLibrary() {
        val declaredActions = globalContext.getAllDeclaredActions()
        library.declaredActionReferences.addAll(declaredActions.map { action -> action.getReference(context) })
    }
}

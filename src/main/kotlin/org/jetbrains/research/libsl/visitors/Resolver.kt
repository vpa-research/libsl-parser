package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jetbrains.research.libsl.LibSLBaseVisitor
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.*
import org.jetbrains.research.libsl.asg.Bool
import java.io.File

class Resolver(
    private val context: LslContext,
    private val basePath: String
    ) : LibSLBaseVisitor<Unit>() {
    private val asgBuilderVisitor = ASGBuilder(context)

    override fun visitFile(ctx: LibSLParser.FileContext) {
        ctx.globalStatement().mapNotNull { it.importStatement() }.forEach { visitImportStatement(it) }

        val typeSections = ctx.globalStatement().mapNotNull { it.typesSection() }
        if (typeSections.size > 1) {
            error("only one types section could be provided")
        }
        for (statement in ctx.globalStatement()) {
            when {
                statement.typesSection() != null -> visitTypesSection(statement.typesSection())
                statement.enumBlock() != null -> visitEnumBlock(statement.enumBlock())
                statement.typeDefBlock() != null -> visitTypeDefBlock(statement.typeDefBlock())
                statement.typealiasStatement() != null -> visitTypealiasStatement(statement.typealiasStatement())
            }
        }

        val automata = ctx.globalStatement().mapNotNull { it.declaration()?.automatonDecl() }
        for (automatonCtx in automata) {
            val typeName = automatonCtx.type.text
            val type = context.resolveType(typeName) ?: error("unresolved type: $typeName")

            val variables = automatonCtx.automatonStatement().mapNotNull { it.variableDeclaration() }.map { variable ->
                val variableName = variable.nameWithType().name.text
                val variableTypeName = variable.nameWithType().type.text
                val variableType = context.resolveType(variableTypeName) ?: error("unresolved type")

                AutomatonVariableDeclaration(
                    variableName,
                    variableType,
                    null
                )
            }

            val constructorVariables = automatonCtx.nameWithType().map { cVar ->
                val argName = cVar.name.text
                val argTypeName = cVar.type.text
                val argType = context.resolveType(argTypeName) ?: error("unresolved type $argTypeName")

                ConstructorArgument(
                    argName,
                    argType
                )
            }

            val states = automatonCtx.automatonStatement()?.filter { it.automatonStateDecl() != null }?.flatMap { statesCtx ->
                statesCtx.automatonStateDecl().identifierList().Identifier().map { stateCtx ->
                    val keyword = statesCtx.start.text
                    val stateName = stateCtx.text
                    val stateKind = StateKind.fromString(keyword)
                    State(stateName, stateKind)
                }
            }.orEmpty()

            val automaton = Automaton(
                automatonCtx.name.text,
                type,
                states,
                listOf(),
                variables,
                constructorVariables,
                listOf(),
            )

            context.storeResolvedAutomaton(automaton)
            variables.forEach { it.automaton = automaton }
            constructorVariables.forEach { it.automaton = automaton }
        }

        ctx.globalStatement().mapNotNull { it.declaration()?.variableDeclaration() }.map { variableDecl ->
            val nameWithType = variableDecl.nameWithType()
            val type = context.resolveType(nameWithType.type.text) ?: error("unresolved type: ${nameWithType.type.text}")
            val init = if (variableDecl.assignmentRight() != null){
                asgBuilderVisitor.processAssignmentRight(variableDecl.assignmentRight())
            } else {
                error("global variables must be initialized in their declarations")
            }

            val variable = GlobalVariableDeclaration(
                nameWithType.name.text,
                type,
                init
            )
            context.storeGlobalVariable(variable)
        }

        for (automaton in automata) {
            visitAutomatonDecl(automaton)
        }

        for (extensionFunction in ctx.globalStatement().mapNotNull { it.declaration()?.functionDecl() }) {
            visitFunctionDecl(extensionFunction)
        }
    }

    override fun visitTypesSection(ctx: LibSLParser.TypesSectionContext) {
        for (semanticTypeContext in ctx.semanticType()) {
            val type = when {
                semanticTypeContext.simpleSemanticType() != null -> {
                    val semanticType = semanticTypeContext.simpleSemanticType().semanticName.text
                    val realTypeCtx = semanticTypeContext.simpleSemanticType().realName
                    val resolvedRealType = context.resolveType(realTypeCtx.text)
                        ?: processRealTypeIdentifier(realTypeCtx)
                    val isPointer = realTypeCtx.asterisk != null

                    SimpleType(semanticType, resolvedRealType, isPointer, context)
                }
                semanticTypeContext.blockType() != null -> {
                    val blockType = semanticTypeContext.blockType()
                    val semanticType = blockType.semanticName.text
                    val realName = blockType.realName
                    val resolvedRealType = context.resolveType(realName.text)
                        ?: processRealTypeIdentifier(realName)
                    val body = blockType.blockTypeStatement().map { statement ->
                        statement.Identifier().text to resolvePrimitiveLiteral(statement.expressionAtomic().primitiveLiteral())
                    }
                    // val genericTypeCtx = blockType.typeIdentifier().generic
                    // todo? val genericType = genericTypeCtx?.let { processRealTypeIdentifier(it) }
                    EnumLikeSemanticType(semanticType, resolvedRealType, body, context)
                }
                else -> error("unknown type's type")
            }

            context.storeResolvedType(type)
        }
    }

    override fun visitTypealiasStatement(ctx: LibSLParser.TypealiasStatementContext) {
        val name = ctx.left.text
        val resolvedRealType = context.resolveType(ctx.right.text)
            ?: processRealTypeIdentifier(ctx.right)
        context.storeResolvedType(TypeAlias(
            name,
            resolvedRealType,
            context
        ))
    }

    override fun visitTypeDefBlock(ctx: LibSLParser.TypeDefBlockContext) {
        val name = ctx.name.name.text
        val typeIdentifier = ctx.typeIdentifier()
        val resolvedRealType = context.resolveType(typeIdentifier.text)
            ?: processRealTypeIdentifier(typeIdentifier)
        val generic = if (typeIdentifier.generic != null) {
            processRealTypeIdentifier(typeIdentifier.generic)
        } else {
            null
        }

        val entries = ctx.typeDefBlockStatement().map { statement ->
            statement.nameWithType().let { it.name.text to processRealTypeIdentifier(it.type) }
        }

        context.storeResolvedType(StructuredType(
            name,
            resolvedRealType,
            generic,
            entries,
            context
        ))
    }

    private fun resolvePrimitiveLiteral(primitiveLiteralContext: LibSLParser.PrimitiveLiteralContext): Atomic {
        return when {
            primitiveLiteralContext.bool != null -> {
                if (primitiveLiteralContext.bool.text == "true") {
                    Bool(true)
                } else {
                    Bool(false)
                }
            }
            primitiveLiteralContext.QuotedString() != null -> {
                val literal = primitiveLiteralContext.QuotedString().text.removeQuotes()
                StringValue(literal)
            }
            primitiveLiteralContext.floatNumber() != null -> {
                FloatNumber(primitiveLiteralContext.floatNumber().text.toFloat())
            }
            primitiveLiteralContext.integerNumber() != null -> {
                IntegerNumber(primitiveLiteralContext.integerNumber().text.toInt())
            }
            else -> error("unknown primitive literal type")
        }
    }

    private fun processRealTypeIdentifier(ctx: LibSLParser.TypeIdentifierContext): RealType {
        val name = ctx.periodSeparatedFullName().Identifier().map { it.text }
        val generic = ctx.generic?.let { processRealTypeIdentifier(it) }
        val isPointer = ctx.asterisk != null

        return RealType(
            name,
            isPointer,
            generic,
            context
        )
    }

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val name = ctx.name.text
        val automaton = context.resolveAutomaton(name) ?: error("")

        ctx.automatonStatement()
            .mapNotNull { it.variableDeclaration() }
            .forEach { decl ->
                val variableName = decl.nameWithType().name.text
                val automatonVariable = automaton.internalVariables.first { it.name == variableName }

                if (decl.assignmentRight() != null) {
                    automatonVariable.initValue = asgBuilderVisitor.processAssignmentRight(decl.assignmentRight())
                }
            }

        context.storeResolvedAutomaton(automaton)

        for (functionDecl in ctx.automatonStatement().mapNotNull { it.functionDecl() }) {
            visitFunctionDecl(functionDecl)
        }
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val (automatonName, name) = parseFunctionName(ctx)
        automatonName ?: error("automaton name not specified for function: $name")

        val typeName = ctx.functionType?.text
        val returnType = if (typeName != null) context.resolveType(typeName)
            ?: error("unresolved type: $typeName") else null

        val args = ctx.functionDeclArgList()?.parameter()?.map { arg ->
            val argType = context.resolveType(arg.type.text) ?: error("unresolved type")
            FunctionArgument(arg.name.text, argType)
        }?.toList().orEmpty()

        val func = Function(name, automatonName, args, returnType, listOf(), listOf(), context)
        context.storeResolvedFunction(func)

        args.forEach { it.function = func }
    }

    override fun visitEnumBlock(ctx: LibSLParser.EnumBlockContext) {
        val semanticType = ctx.typeIdentifier().text
        val body = ctx.enumBlockStatement().map { statement ->
            statement.Identifier().text to IntegerNumber(statement.integerNumber().text.toInt())
        }
        context.storeResolvedType(
            EnumType(
                semanticType,
                body,
                context
            ))
    }

    override fun visitImportStatement(ctx: LibSLParser.ImportStatementContext) {
        // todo: forbid for recursive imports
        val importString = ctx.importString.text.removeQuotes()
        val filePath = "$basePath/$importString.lsl"
        val file = File(filePath)

        if (!file.exists()) {
            error("unresolved import path $importString. Full path: $filePath")
        }

        val stream = CharStreams.fromString(file.readText())
        val lexer = LibSLLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = LibSLParser(tokenStream)

        val newContext = LslContext()
        context.import(newContext)
        val resolver = Resolver(newContext, basePath)
        val fileCtx = parser.file()
        resolver.visitFile(fileCtx)
        val asgBuilder = ASGBuilder(context)
        asgBuilder.visitFile(fileCtx)
    }
}

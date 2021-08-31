package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.TerminalNode
import org.jetbrains.research.libsl.LibSLBaseVisitor
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.*
import org.jetbrains.research.libsl.asg.BoolLiteral
import java.io.File

class Resolver(
    private val context: LslContext,
    private val basePath: String
    ) : LibSLBaseVisitor<Unit>() {
    private val asgBuilderVisitor = ASGBuilder(context)

    override fun visitFile(ctx: LibSLParser.FileContext) {
        ctx.globalStatement().mapNotNull { it.ImportStatement() }.forEach { processImportStatement(it) }

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

        val automata = ctx.globalStatement().mapNotNull { it.topLevelDecl()?.automatonDecl() }
        for (automatonCtx in automata) {
            val typeName = automatonCtx.type.processIdentifier()
            val type = context.resolveType(typeName) ?: error("unresolved type: $typeName")

            val variables = automatonCtx.automatonStatement().mapNotNull { it.variableDecl() }.map { variable ->
                val variableName = variable.nameWithType().name.processIdentifier()
                val variableTypeName = variable.nameWithType().type.text
                val variableType = context.resolveType(variableTypeName) ?: error("unresolved type $variableTypeName")

                AutomatonVariableDeclaration(
                    variableName,
                    variableType,
                    null
                )
            }

            val constructorVariables = automatonCtx.nameWithType().map { cVar ->
                val argName = cVar.name.processIdentifier()
                val argTypeName = cVar.type.text
                val argType = context.resolveType(argTypeName) ?: error("unresolved type $argTypeName")

                ConstructorArgument(
                    argName,
                    argType
                )
            }

            val states = automatonCtx.automatonStatement()?.filter { it.automatonStateDecl() != null }?.flatMap { statesCtx ->
                statesCtx.automatonStateDecl().identifierList().Identifier().map { stateCtx ->
                    val keyword = statesCtx.start.processIdentifier()
                    val stateName = stateCtx.processIdentifier()
                    val stateKind = StateKind.fromString(keyword)
                    State(stateName, stateKind)
                }
            }.orEmpty()

            val automaton = Automaton(
                automatonCtx.name.processIdentifier(),
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
            states.forEach { it.automaton = automaton }
        }

        ctx.globalStatement().mapNotNull { it.topLevelDecl()?.variableDecl() }.map { variableDecl ->
            val nameWithType = variableDecl.nameWithType()
            val type = context.resolveType(nameWithType.type.text) ?: error("unresolved type: ${nameWithType.type.text}")
            val init = if (variableDecl.assignmentRight() != null){
                asgBuilderVisitor.processAssignmentRight(variableDecl.assignmentRight())
            } else {
                error("global variables must be initialized in their declarations")
            }

            val variable = GlobalVariableDeclaration(
                nameWithType.name.processIdentifier(),
                type,
                init
            )
            context.storeGlobalVariable(variable)
        }

        for (automaton in automata) {
            visitAutomatonDecl(automaton)
        }

        for (extensionFunction in ctx.globalStatement().mapNotNull { it.topLevelDecl()?.functionDecl() }) {
            visitFunctionDecl(extensionFunction)
        }
    }

    override fun visitTypesSection(ctx: LibSLParser.TypesSectionContext) {
        for (semanticTypeContext in ctx.semanticTypeDecl()) {
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
                    val semanticType = blockType.semanticName.processIdentifier()
                    val realName = blockType.realName
                    val resolvedRealType = context.resolveType(realName.text)
                        ?: processRealTypeIdentifier(realName)
                    val body = blockType.blockTypeStatement().map { statement ->
                        statement.Identifier().processIdentifier() to resolvePrimitiveLiteral(statement.expressionAtomic().primitiveLiteral())
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
        val name = ctx.name.name.processIdentifier()
        val typeIdentifier = ctx.typeIdentifier()
        val resolvedRealType = context.resolveType(typeIdentifier.text)
            ?: processRealTypeIdentifier(typeIdentifier)
        val generic = if (typeIdentifier.generic != null) {
            processRealTypeIdentifier(typeIdentifier.generic)
        } else {
            null
        }

        val entries = ctx.typeDefBlockStatement().map { statement ->
            statement.nameWithType().let { it.name.processIdentifier() to processRealTypeIdentifier(it.type) }
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
                if (primitiveLiteralContext.bool.processIdentifier() == "true") {
                    BoolLiteral(true)
                } else {
                    BoolLiteral(false)
                }
            }
            primitiveLiteralContext.DoubleQuotedString() != null -> {
                val literal = primitiveLiteralContext.DoubleQuotedString().processIdentifier().removeDoubleQuotes()
                StringLiteral(literal)
            }
            primitiveLiteralContext.floatNumber() != null -> {
                FloatLiteral(primitiveLiteralContext.floatNumber().text.toFloat())
            }
            primitiveLiteralContext.integerNumber() != null -> {
                IntegerLiteral(primitiveLiteralContext.integerNumber().text.toInt())
            }
            else -> error("unknown primitive literal type")
        }
    }

    private fun processRealTypeIdentifier(ctx: LibSLParser.TypeIdentifierContext): Type {
        val name = ctx.periodSeparatedFullName().Identifier().map { it.processIdentifier() }
        val generic = ctx.generic?.let { processRealTypeIdentifier(it) }
        val isPointer = ctx.asterisk != null

        return if (name.size == 1 && name.first() == "array" && generic != null) {
            ArrayType(
                name.first(),
                isPointer,
                generic,
                context
            )
        } else {
            return context.resolveType(name.joinToString("."))
                ?: RealType(
                name,
                isPointer,
                generic,
                context
            )
        }
    }

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val name = ctx.name.processIdentifier()
        val automaton = context.resolveAutomaton(name) ?: error("")

        ctx.automatonStatement()
            .mapNotNull { it.variableDecl() }
            .forEach { decl ->
                val variableName = decl.nameWithType().name.processIdentifier()
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

        val typeName = ctx.functionType?.processIdentifier()
        val returnType = if (typeName != null) context.resolveType(typeName)
            ?: error("unresolved type: $typeName") else null

        var argumentIndex = 0
        val args = ctx.functionDeclArgList()?.parameter()?.map { arg ->
            val argType = context.resolveType(arg.type.processIdentifier()) ?: error("unresolved type")
            FunctionArgument(arg.name.processIdentifier(), argType, argumentIndex++,null)
        }?.toList().orEmpty()

        val func = Function(name, automatonName, args, returnType, listOf(), listOf(), context)

        context.storeResolvedFunction(func)

        args.forEach { it.function = func }
        if (returnType != null) {
            val result = ResultVariable(returnType)
            func.resultVariable = result
        }
    }

    override fun visitEnumBlock(ctx: LibSLParser.EnumBlockContext) {
        val semanticType = ctx.typeIdentifier().text
        val body = ctx.enumBlockStatement().map { statement ->
            statement.Identifier().processIdentifier() to IntegerLiteral(statement.integerNumber().text.toInt())
        }
        context.storeResolvedType(
            EnumType(
                semanticType,
                body,
                context
            ))
    }

    private fun processImportStatement(terminal: TerminalNode) {
        // todo: forbid a recursive imports
        val importString = parseStringTokenStringSemicolon(terminal.processIdentifier(), "import")
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
        newContext.init()
        context.import(newContext)
        val resolver = Resolver(newContext, basePath)
        val fileCtx = parser.file()
        resolver.visitFile(fileCtx)
        val asgBuilder = ASGBuilder(context)
        asgBuilder.visitFile(fileCtx)
    }
}

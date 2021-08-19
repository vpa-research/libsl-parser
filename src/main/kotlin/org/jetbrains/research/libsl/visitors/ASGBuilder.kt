package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.RuleContext
import org.jetbrains.research.libsl.LibSLBaseVisitor
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.*
import org.jetbrains.research.libsl.asg.Function

class ASGBuilder(private val context: LslContext) : LibSLBaseVisitor<Node>() {
    override fun visitFile(ctx: LibSLParser.FileContext): Library {
        val header = ctx.header()
        val libraryName = header.libraryName?.text ?: error("no library name specified")
        val language = header.lang?.text?.removeSurrounding("\"", "\"")
        val libraryVersion = header.ver?.text?.removeSurrounding("\"", "\"")
        val lslVersion = header.lslver?.text?.removeSurrounding("\"", "\"")?.split(".")?.let {
            val parts = it.map { part -> part.toUInt() }
            Triple(parts[0], parts[1], parts[2])
        }
        val url = header?.link?.text?.removeSurrounding("\"", "\"")

        val meta = MetaNode(libraryName, libraryVersion, language, url, lslVersion)
        val imports = ctx.globalStatement().mapNotNull { it.ImportStatement() }.map {
            parseStringTokenStringSemicolon(it.text, "import")
        }.toList()
        val includes = ctx.globalStatement().mapNotNull { it.IncludeStatement() }.map {
            parseStringTokenStringSemicolon(it.text, "include")
        }.toList()

        val automata = ctx.globalStatement()
            .mapNotNull { it.declaration()?.automatonDecl() }
            .map { visitAutomatonDecl(it) }
            .toMutableList()
        val nonlocalFunctions = ctx.globalStatement()
            .mapNotNull { it.declaration()?.functionDecl() }
            .map { visitFunctionDecl(it) }

        val types = context.typeStorage.map { it.value }

        val library = Library(
            meta,
            imports,
            includes,
            types,
            automata,
            nonlocalFunctions.fold(mutableMapOf<String, MutableList<Function>>()) { old, curr ->
                    old.getOrPut(curr.automatonName) { mutableListOf()}.add(curr); old
                },
            context.globalVariables
        )

        automata.forEach { it.parent.node = library }
        nonlocalFunctions.forEach { it.parent.node = library }

        return library
    }

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext): Automaton {
        val name = ctx.name.text
        val automaton = context.resolveAutomaton(name)
        val states = automaton!!.states
        val shifts = ctx.automatonStatement()?.filter { it.automatonShiftDecl() != null }?.flatMap { shiftCtx ->
            val parsedShift = shiftCtx.automatonShiftDecl()
            val toName = parsedShift.to?.text!!
            if (parsedShift.identifierList() != null) {
                parsedShift.identifierList().Identifier().map { fromIdentifier ->
                    val fromName = fromIdentifier.text!!

                    processShift(fromName, toName, shiftCtx, states, automatonName = name)
                }
            } else {
                val fromName = parsedShift.from.text!!
                listOf(processShift(fromName, toName, shiftCtx, states, automatonName = name))
            }
        }.orEmpty()

        val functions = ctx.automatonStatement()
            .mapNotNull { it.functionDecl() }.map { visitFunctionDecl(it) }

        automaton.apply {
            this.shifts = shifts
            this.localFunctions = functions
        }

        functions.forEach { it.parent.node = automaton }

        return automaton
    }

    private fun processShift(
        fromName: String,
        toName: String,
        shiftCtx: LibSLParser.AutomatonStatementContext,
        states: List<State>,
        automatonName: String
    ): Shift {
        val parsedShift = shiftCtx.automatonShiftDecl()
        val fromState = if (fromName == "any") {
            State("any", StateKind.SIMPLE, isAny = true)
        } else {
            states.firstOrNull { it.name == fromName } ?: error("unknown state")
        }

        val toState = if (toName == "self") {
            State("self", StateKind.SIMPLE, isSelf = true)
        } else {
            states.firstOrNull { it.name == toName } ?: error("unknown state")
        }

        val functions = parsedShift.functionsList()?.functionsListPart()?.map { part ->
            val functionName = part.name.text
            val functionArgs = part.Identifier().drop(1).map { type ->
                context.resolveType(type.text) ?: error("unresolved type: ${type.text}")
            }

            context.resolveFunction(functionName, automatonName = automatonName, argsType=functionArgs)
                ?: context.resolveFunction(functionName, automatonName = automatonName)
                ?: error("unresolved function: $functionName")
        } ?: error("empty functions list in shift $fromName -> $toName")

        return Shift(
            fromState,
            toState,
            functions
        )
    }

    override fun visitAssignmentRight(ctx: LibSLParser.AssignmentRightContext): Expression = when {
        ctx.expression() != null -> {
            visitExpression(ctx.expression())
        }
        ctx.callAutomatonConstructorWithNamedArgs() != null -> {
            visitCallAutomatonConstructorWithNamedArgs(ctx.callAutomatonConstructorWithNamedArgs())
        }
        else -> error("can't parse init value in variable")
    }

    override fun visitCallAutomatonConstructorWithNamedArgs(ctx: LibSLParser.CallAutomatonConstructorWithNamedArgsContext): Atomic {
        val calleeName = ctx.name.text
        val calleeAutomaton = context.resolveAutomaton(calleeName) ?: error("can't resolve automaton $calleeName")

        val args = ctx.namedArgs()?.argPair()?.mapNotNull { pair ->
            val name = pair.name.text
            if (name == "state") {
                null
            } else {
                val targetVariable = calleeAutomaton.constructorVariables.first { it.name == name }
                val value = visitExpression(pair.expression())
                ArgumentWithValue(targetVariable, value)
            }
        }.orEmpty()

        val stateName = ctx.namedArgs()?.argPair()?.firstOrNull { it.name.text == "state" }?.expressionAtomic()?.text
        val state = calleeAutomaton.states.firstOrNull { it.name == stateName } ?: error("unresolved state: $stateName")

        return CallAutomatonConstructor(calleeAutomaton, args, state)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext): Function {
        val (ownerAutomatonName, functionName) = parseFunctionName(ctx)
        var argIndex = 0
        val args = ctx.functionDeclArgList()?.parameter()?.map { arg ->
            val argName = arg.name.text
            val argType = context.resolveType(arg.type.text) ?: error("unresolved type")
            val annotation = arg.annotation()?.let { anno ->
                Annotation(anno.Identifier().text, anno.valuesAndIdentifiersList()?.expression()?.map { atomic ->
                    visitExpression(atomic)
                }.orEmpty())
            }

            FunctionArgument(argName, argType, ++argIndex, annotation)
        }.orEmpty()
        val typeName = ctx.functionType?.text
        val type = if (typeName != null) context.resolveType(typeName) ?: error("unresolved type: $typeName") else null

        val preamble = ctx.functionPreamble()?.preamblePart()?.map { part ->
            when {
                part.requiresContract() != null -> {
                    val contractName = part.requiresContract().name?.text
                    val contractExpression = visitExpression(part.requiresContract().expression())
                    Contract(contractName, contractExpression, ContractKind.REQUIRES)
                }
                part.ensuresContract() != null -> {
                    val contractName = part.ensuresContract().name?.text
                    val contractExpression = visitExpression(part.ensuresContract().expression())
                    Contract(contractName, contractExpression, ContractKind.ENSURES)
                }
                else -> error("unknown function statement's type: $ownerAutomatonName.$functionName")
            }
        }.orEmpty()

        val statementList = ctx.functionBody()?.functionBodyStatements()?.map { variableStatement ->
            when {
                variableStatement.variableAssignment() != null -> {
                    val variableAssignment = variableStatement.variableAssignment()
                    val value = visitAssignmentRight(variableAssignment.assignmentRight())
                    val variable = visitQualifiedAccess(variableAssignment.qualifiedAccess())

                    Assignment(variable, value)
                }

                variableStatement.action() != null -> {
                    val action = variableStatement.action()
                    val actionName = action.Identifier().text
                    val actionArgs = action
                        .valuesAndIdentifiersList()
                        ?.expression()
                        ?.map { visitExpression(it) }
                        .orEmpty()

                    Action(
                        actionName,
                        actionArgs
                    )
                }

                else -> error("unknown statement type")
            }

        }.orEmpty()

        val targetAnnotation = args.firstOrNull { it.annotation?.name == "target" }
        val resolvedFunction = context.resolveFunction(
            functionName,
            automatonName=ownerAutomatonName,
            argsType = args.map { it.type },
            returnType = type
        ) ?: error("error on parsing function: $ownerAutomatonName.$functionName")

        if (targetAnnotation != null) {
            val target = context.resolveAutomaton(targetAnnotation.name) ?: error("unresolved automaton: $targetAnnotation")
            resolvedFunction.target = target
        } else {
            val automatonName = resolvedFunction.automatonName
            resolvedFunction.target = context.resolveAutomaton(automatonName) ?: error("unresolved automaton: $automatonName")
        }

        return resolvedFunction.apply {
            contracts = preamble
            statements = statementList
        }
    }

    override fun visitEnsuresContract(ctx: LibSLParser.EnsuresContractContext): Contract {
        return Contract(
            name = ctx.name?.text,
            expression = visitExpression(ctx.expression()),
            kind = ContractKind.ENSURES
        )
    }

    override fun visitRequiresContract(ctx: LibSLParser.RequiresContractContext): Contract {
        return Contract(
            name = ctx.name?.text,
            expression = visitExpression(ctx.expression()),
            kind = ContractKind.REQUIRES
        )
    }

    override fun visitExpression(ctx: LibSLParser.ExpressionContext): Expression {
        return when {
            ctx.apostrophe != null -> OldValue(visitQualifiedAccess(ctx.qualifiedAccess()))
            ctx.UNARY_MINUS() != null -> {
                val content = visitExpression(ctx.expression(0))

                UnaryOpExpression(content, ArithmeticUnaryOp.MINUS)
            }
            ctx.INV() != null -> {
                val content = visitExpression(ctx.expression(0))

                UnaryOpExpression(content, ArithmeticUnaryOp.INVERSION)
            }
            ctx.expressionAtomic() != null -> visitExpressionAtomic(ctx.expressionAtomic())
            ctx.qualifiedAccess() != null -> visitQualifiedAccess(ctx.qualifiedAccess())
            ctx.lbracket != null -> visitExpression(ctx.expression().first())
            else -> {
                val left = visitExpression(ctx.expression(0))
                val right = visitExpression(ctx.expression(1))
                val op = ArithmeticBinaryOps.fromString(ctx.op.text)

                BinaryOpExpression(left, right, op)
            }
        }
    }

    internal fun processAssignmentRight(ctx: LibSLParser.AssignmentRightContext) = when {
        ctx.expression() != null -> {
            visitExpression(ctx.expression())
        }
        ctx.callAutomatonConstructorWithNamedArgs() != null -> {
            visitCallAutomatonConstructorWithNamedArgs(ctx.callAutomatonConstructorWithNamedArgs())
        }
        else -> error("can't parse init value in variable")
    }

    override fun visitExpressionAtomic(ctx: LibSLParser.ExpressionAtomicContext): Atomic = when {
        ctx.primitiveLiteral()?.integerNumber() != null -> IntegerNumber(ctx.primitiveLiteral().integerNumber().text.toInt())
        ctx.primitiveLiteral()?.floatNumber() != null -> FloatNumber(ctx.primitiveLiteral().floatNumber().text.toFloat())
        ctx.primitiveLiteral()?.DoubleQuotedString() != null -> StringValue(ctx.primitiveLiteral().DoubleQuotedString().text.removeDoubleQuotes())
        ctx.qualifiedAccess() != null -> visitQualifiedAccess(ctx.qualifiedAccess())
        else -> error("unknown atomic type")
    }

    override fun visitQualifiedAccess(ctx: LibSLParser.QualifiedAccessContext): QualifiedAccess {
        return when {
            ctx.periodSeparatedFullName() != null -> {
                val names = ctx.periodSeparatedFullName().Identifier().map { it.text }
                val name = names.first()

                val variable = resolveVariableDependingOnContext(ctx, name, context) ?: error("can't resolve variable: $name")
                val baseType = variable.type
                VariableAccess(
                    name,
                    resolvePeriodSeparatedChain(baseType, names.drop(1)),
                    baseType,
                    variable
                )
            }

            ctx.simpleCall() != null -> {
                val automatonName = ctx.simpleCall().Identifier(0).text
                val automaton = context.resolveAutomaton(automatonName) ?: error("unresolved automaton: $automatonName")
                val functionCtx = ctx.getParentOfType<LibSLParser.FunctionDeclContext>() ?: error("access not in function")
                val function = resolveFunctionByCtx(functionCtx) ?: error("unresolved function")

                val argName = ctx.simpleCall().Identifier(1).text
                val arg = function.args.firstOrNull { it.name == argName } ?: error("unresolved argument: $argName")

                AutomatonGetter(
                    automaton,
                    arg,
                    visitQualifiedAccess(ctx.qualifiedAccess())
                )
            }

            ctx.qualifiedAccess() != null -> {
                val parentQualifiedAccess = visitQualifiedAccess(ctx.qualifiedAccess())

                return if (ctx.expressionAtomic() != null) {
                    val indexCtx = ctx.expressionAtomic()
                    val index = visitExpressionAtomic(indexCtx)

                    parentQualifiedAccess.apply {
                        val child = ArrayAccess(
                            index,
                            lastChild.type
                        )

                        if (!(lastChild.type.isArray || lastChild is ArrayAccess && lastChild.type.isArray) ) {
                            error("variable access can't be performed on non-array type: ${parentQualifiedAccess.type.name}")
                        }
                        lastChild.childAccess = child
                    }
                } else {
                    parentQualifiedAccess
                }
            }
            else -> error("unknown qualified access kind")
        }
    }

    private fun resolvePeriodSeparatedChain(parentType: Type, names: List<String>): QualifiedAccess? {
        if (names.isEmpty()) return null
        val name = names.first()
        return when(parentType) {
            is StructuredType -> {
                val entry = parentType.entries.firstOrNull { it.first == name }
                    ?: error("unresolved field $name in type ${parentType.name}")
                VariableAccess(
                    name,
                    resolvePeriodSeparatedChain(entry.second, names.drop(1)),
                    entry.second,
                    null
                )
            }
            is EnumType -> {
                val entry = parentType.entries.firstOrNull { it.first == name }
                    ?: error("unresolved field $name in type ${parentType.name}")
                VariableAccess(
                    entry.first,
                    null,
                    parentType,
                    null
                )
            }
            is EnumLikeSemanticType -> {
                val entry = parentType.entries.firstOrNull { it.first == name }
                    ?: error("unresolved field $name in type ${parentType.name}")
                VariableAccess(
                    entry.first,
                    null,
                    parentType,
                    null
                )
            }
            is TypeAlias -> {
                resolvePeriodSeparatedChain(parentType.originalType, names.drop(1))
            }
            else -> {
                if (names.size == 1) {
                    VariableAccess(
                        name,
                        null,
                        parentType.resolveFieldType(name) ?: error("can't resolve field $name in type ${parentType.name}"),
                        null
                    )
                } else {
                    error("can't resolve access chain. Unsupported part type: ${parentType::class.java}")
                }
            }
        }
    }


    private fun getParentAutomatonOrNull(ctx: RuleContext): Automaton? {
        return when {
            ctx.parent == null -> {
                null
            }
            ctx is LibSLParser.FunctionDeclContext -> {
                val automatonName = resolveFunctionByCtx(ctx)?.automatonName ?: error("can't resolve function: ${ctx.name.text}")
                context.resolveAutomaton(automatonName) ?: error("can't resolve automaton: $automatonName")
            }
            ctx is LibSLParser.AutomatonDeclContext -> {
                val name = ctx.name.text
                context.resolveAutomaton(name) ?: error("can't resolve automaton: $name")
            }
            else -> {
                getParentAutomatonOrNull(ctx.parent)
            }
        }
    }

    private fun resolveFunctionByCtx(ctx: LibSLParser.FunctionDeclContext): Function? {
        var argumentIndex = 0
        val args = ctx.functionDeclArgList()?.parameter()?.map { arg ->
            val argName = arg.name.text
            val argType = context.resolveType(arg.type.text) ?: error("unresolved type")
            val annotation = arg.annotation()?.let { anno ->
                Annotation(anno.Identifier().text, anno.valuesAndIdentifiersList().expression().map { atomic ->
                    visitExpression(atomic)
                })
            }
            FunctionArgument(argName, argType, ++argumentIndex, annotation)
        }.orEmpty()

        return if (ctx.name.Identifier().size > 1) {
            // an extension function case
            val automatonName = ctx.name.Identifier(0)?.text
            val funcName = ctx.name.text.removePrefix("$automatonName.")
            context.resolveFunction(funcName, automatonName, args)
        } else {
            // a standalone function case
            val funcName = ctx.name.Identifier().first().text
            val automaton = ctx.parent?.parent as? LibSLParser.AutomatonDeclContext
                ?: error("non-extension function $funcName not in automaton")
            val automatonName = automaton.name.text
            context.resolveFunction(funcName, automatonName, args)
        }
    }

    private fun resolveVariableDependingOnContext(ctx: RuleContext, variableName: String, context: LslContext): Variable? {
        val res = when (ctx) {
            is LibSLParser.FunctionDeclContext -> {
                val func = resolveFunctionByCtx(ctx)!!
                if (variableName == "result") {
                    func.resultVariable
                } else {
                    func.args.firstOrNull { it.name == variableName }
                        ?: func.automaton.variables.firstOrNull {
                            it.name == variableName
                        }
                }
            }
            is LibSLParser.AutomatonDeclContext -> {
                val automatonName = ctx.name.text
                val automaton = context.resolveAutomaton(automatonName)!!
                automaton.variables.firstOrNull { it.name == variableName }
            }
            else -> null
        }
        return if (ctx.parent == null) {
            context.resolveVariable(variableName)
        } else {
            res ?: resolveVariableDependingOnContext(ctx.parent, variableName, context)
        }
    }
}

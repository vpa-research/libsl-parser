package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.RuleContext
import org.jetbrains.research.libsl.LibSLBaseVisitor
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.*
import org.jetbrains.research.libsl.asg.Function

class ASGBuilder(private val context: LslContext) : LibSLBaseVisitor<Node>() {
    private val syntheticVariable = Variable("state",
        SyntheticType("state", RealType(listOf(), listOf()), context),
        null
    )
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
        val imports = ctx.imports().oneImport().map { it.importString.text.removeSurrounding("\"", "\"") }.toList()
        val includes = ctx.includes().include().map { it.includeString.text.removeSurrounding("\"", "\"") }.toList()

        val automata = ctx.declarations().declaration()
            .mapNotNull { it.automatonDecl() }
            .map { visitAutomatonDecl(it) }
            .toMutableList()
        val nonlocalFunctions = ctx.declarations().declaration()
            .mapNotNull { it.functionDecl() }
            .map { visitFunctionDecl(it) }

        val types = ctx.typesSection().typesSectionBody().semanticType().map { typeCtx ->
            context.resolveType(typeCtx.semanticTypeName) ?: error("unresolved type: ${typeCtx.text}")
        }

        val library = Library(
            meta,
            imports,
            includes,
            types,
            automata,
            nonlocalFunctions.filter { it.automatonName != null }
                .fold(mutableMapOf<String, MutableList<Function>>()) { old, curr ->
                    old.getOrPut(curr.automatonName!!) { mutableListOf()}.add(curr); old
                },
            context.globalVariables
        )

        automata.forEach { it.parent.node = library }
        nonlocalFunctions.forEach { it.parent.node = library }

        return library
    }

    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext): Automaton {
        val name = ctx.name.text
        val states = ctx.automatonStatement()?.filter { it.automatonStateDecl() != null }?.flatMap { statesCtx ->
            statesCtx.automatonStateDecl().identifierList().Identifier().map { stateCtx ->
                val keyword = statesCtx.start.text
                val stateName = stateCtx.text
                val stateKind = StateKind.fromString(keyword)
                State(stateName, stateKind)
            }
        }.orEmpty()
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

        val automaton = context.resolveAutomaton(name)?.apply {
            this.states = states
            this.shifts = shifts
            this.localFunctions = functions
        } ?: error("")

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

    override fun visitAssignmentRight(ctx: LibSLParser.AssignmentRightContext): Atomic = when {
        ctx.expressionAtomic() != null -> {
            visitExpressionAtomic(ctx.expressionAtomic())
        }
        ctx.callAutomatonWithNamedArgs() != null -> {
            visitCallAutomatonWithNamedArgs(ctx.callAutomatonWithNamedArgs())
        }
        else -> error("can't parse init value in variable")
    }

    override fun visitCallAutomatonWithNamedArgs(ctx: LibSLParser.CallAutomatonWithNamedArgsContext): Atomic {
        val calleeName = ctx.name.text
        val calleeAutomaton = context.resolveAutomaton(calleeName) ?: error("can't resolve automaton $calleeName")

        val args = ctx.namedArgs()?.argPair()?.map { pair ->
            val name = pair.name.text
            val targetVariable = if (name == "state") {
                syntheticVariable
            } else {
                calleeAutomaton.constructorVariables.first { it.name == name }
            }
            val value = visitExpressionAtomic(pair.value)

            ArgumentWithValue(targetVariable, value)
        }.orEmpty()

        return CallAutomatonConstructor(calleeAutomaton, args)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext): Function {
        val (ownerAutomatonName, functionName) = parseFunctionName(ctx)
        val args = ctx.functionDeclArgList()?.parameter()?.map { arg ->
            val argName = arg.name.text
            val argType = context.resolveType(arg.type.text) ?: error("unresolved type")

            Argument(argName, argType)
        }.orEmpty()
        val typeName = ctx.functionType?.text
        val type = if (typeName != null) context.resolveType(typeName) ?: error("unresolved type: $typeName") else null

        val preamble = ctx.functionPreamble()?.preamblePart()?.map { part ->
            when {
                part.requiresContract() != null -> {
                    val contractName = part.requiresContract().name?.text
                    val contractExpression = visitContractExpression(part.requiresContract().contractExpression())
                    Contract(contractName, contractExpression, ContractKind.REQUIRES)
                }
                part.ensuresContract() != null -> {
                    val contractName = part.ensuresContract().name?.text
                    val contractExpression = visitContractExpression(part.ensuresContract().contractExpression())
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
                        ?.expressionAtomic()
                        ?.map { visitExpressionAtomic(it) }
                        .orEmpty()

                    Action(
                        actionName,
                        actionArgs
                    )
                }

                else -> error("unknown statement type")
            }

        }.orEmpty()

        val resolvedFunction = context.resolveFunction(functionName, automatonName=ownerAutomatonName, args = args, returnType = type)
            ?: error("error on parsing function: $ownerAutomatonName.$functionName")

        return resolvedFunction.apply {
            contracts = preamble
            statements = statementList
        }
    }

    override fun visitEnsuresContract(ctx: LibSLParser.EnsuresContractContext): Contract {
        return Contract(
            name = ctx.name?.text,
            expression = visitContractExpression(ctx.contractExpression()),
            kind = ContractKind.ENSURES
        )
    }

    override fun visitRequiresContract(ctx: LibSLParser.RequiresContractContext): Contract {
        return Contract(
            name = ctx.name?.text,
            expression = visitContractExpression(ctx.contractExpression()),
            kind = ContractKind.REQUIRES
        )
    }

    override fun visitContractExpression(ctx: LibSLParser.ContractExpressionContext): Expression {
        return when {
            ctx.apostrophe != null -> OldValue(visitQualifiedAccess(ctx.qualifiedAccess()))
            ctx.UNARY_MINUS() != null -> {
                val content = visitContractExpression(ctx.contractExpression(0))

                UnaryOpExpression(content, ArithmeticUnaryOp.MINUS)
            }
            ctx.INV() != null -> {
                val content = visitContractExpression(ctx.contractExpression(0))

                UnaryOpExpression(content, ArithmeticUnaryOp.INVERSION)
            }
            ctx.expressionAtomic() != null -> visitExpressionAtomic(ctx.expressionAtomic())
            ctx.qualifiedAccess() != null -> visitQualifiedAccess(ctx.qualifiedAccess())
            else -> {
                val left = visitContractExpression(ctx.contractExpression(0))
                val right = visitContractExpression(ctx.contractExpression(1))
                val op = ArithmeticBinaryOps.fromString(ctx.op.text)

                BinaryOpExpression(left, right, op)
            }
        }
    }

    internal fun processAssignmentRight(ctx: LibSLParser.AssignmentRightContext) = when {
        ctx.expressionAtomic() != null -> {
            visitExpressionAtomic(ctx.expressionAtomic())
        }
        ctx.callAutomatonWithNamedArgs() != null -> {
            visitCallAutomatonWithNamedArgs(ctx.callAutomatonWithNamedArgs())
        }
        else -> error("can't parse init value in variable")
    }

    override fun visitExpressionAtomic(ctx: LibSLParser.ExpressionAtomicContext): Atomic = when {
        ctx.integerNumber() != null -> IntegerNumber(ctx.integerNumber().text.toInt())
        ctx.floatNumber() != null -> FloatNumber(ctx.floatNumber().text.toFloat())
        ctx.QuotedString() != null -> StringValue(removeQuotes(ctx.QuotedString().text))
        ctx.qualifiedAccess() != null -> visitQualifiedAccess(ctx.qualifiedAccess())
        else -> error("unknown atomic type")
    }

    override fun visitQualifiedAccess(ctx: LibSLParser.QualifiedAccessContext): VariableAccess {
        val name = ctx.periodSeparatedFullName().text
        val automaton = getParentAutomatonOrNull(ctx)

        return if (ctx.integerNumber() != null) {
            val index = ctx.integerNumber().text.toInt()
            VariableAccess(name, automaton, index)
        } else {
            VariableAccess(name, automaton)
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
        val args = ctx.functionDeclArgList()?.parameter()?.map { arg ->
            val argName = arg.name.text
            val argType = context.resolveType(arg.type.text) ?: error("unresolved type")

            Argument(argName, argType)
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
}

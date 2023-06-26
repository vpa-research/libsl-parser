package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.*
import org.jetbrains.research.libsl.LibSLParserBaseVisitor
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.builders.*
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference

class ExpressionVisitor(
    val context: LslContextBase
) : LibSLParserBaseVisitor<Expression>() {
    override fun visitExpression(ctx: ExpressionContext): Expression {
        return when {
            ctx.expression().size == 1 && ctx.op == null -> {
                // brackets expression
                visitExpression(ctx.expression()!![0])
            }

            ctx.expression().size == 2 -> {
                // binary operation
                processBinaryExpression(ctx)
            }

            ctx.op != null -> {
                // unary operation
                processUnaryExpression(ctx)
            }

            ctx.expressionAtomic() != null -> {
                visitExpressionAtomic(ctx.expressionAtomic())
            }

            ctx.apostrophe != null -> {
                processOldValue(ctx.qualifiedAccess())
            }

            ctx.qualifiedAccess() != null -> {
                visitQualifiedAccess(ctx.qualifiedAccess())
            }

            ctx.unaryOp() != null -> {
                visitUnaryOp(ctx.unaryOp())
            }

            ctx.procUsage() != null -> {
                visitProcUsage(ctx.procUsage())
            }

            ctx.actionUsage() != null -> {
                visitActionUsage(ctx.actionUsage())
            }

            else -> error("unknown expression type")
        }
    }

    private fun processBinaryExpression(ctx: ExpressionContext): BinaryOpExpression {
        val opText = when {
            ctx.op != null -> let {
                ctx.op.text
            }
            ctx.bitShiftOp().lShift() != null -> let {
                "<<"
            }
            ctx.bitShiftOp().rShift() != null -> let {
                ">>"
            }
            ctx.bitShiftOp().uRShift() != null -> let {
                ">>>"
            }

            else -> error("unknown binary expression")
        }

        val op = ArithmeticBinaryOps.fromString(opText)

        val left = ctx.expression(0)
        val right = ctx.expression(1)

        return processBinaryExpression(left, right, op)
    }

    private fun processBinaryExpression(
        left: ExpressionContext,
        right: ExpressionContext,
        op: ArithmeticBinaryOps
    ): BinaryOpExpression {
        val leftExpression = visitExpression(left)
        val rightExpression = visitExpression(right)

        return BinaryOpExpression(leftExpression, rightExpression, op)
    }

    private fun processUnaryExpression(ctx: ExpressionContext): UnaryOpExpression {
        val opText = ctx.op.text
        val op = ArithmeticUnaryOp.fromString(opText)
        val expression = visitExpression(ctx.expression(0))

        return UnaryOpExpression(op, expression)
    }

    private fun processOldValue(ctx: QualifiedAccessContext): OldValue {
        val value = visitQualifiedAccess(ctx)
        return OldValue(value)
    }

    override fun visitExpressionAtomic(ctx: LibSLParser.ExpressionAtomicContext): Atomic {
        return when {
            ctx.primitiveLiteral() != null -> {
                visitPrimitiveLiteral(ctx.primitiveLiteral())
            }

            ctx.qualifiedAccess() != null -> {
                visitQualifiedAccess(ctx.qualifiedAccess())
            }

            ctx.arrayLiteral() != null -> {
                visitArrayLiteral(ctx.arrayLiteral())
            }

            else -> error("unknown expression kind")
        }
    }

    override fun visitPrimitiveLiteral(primitiveLiteralContext: LibSLParser.PrimitiveLiteralContext): Atomic {
        return when {
            primitiveLiteralContext.bool != null -> {
                if (primitiveLiteralContext.bool.asPeriodSeparatedString() == "true") {
                    BoolLiteral(true)
                } else {
                    BoolLiteral(false)
                }
            }

            primitiveLiteralContext.DoubleQuotedString() != null -> {
                val literal =
                    primitiveLiteralContext.DoubleQuotedString().asPeriodSeparatedString().removeDoubleQuotes()
                StringLiteral(literal)
            }

            else -> super.visitPrimitiveLiteral(primitiveLiteralContext) as Atomic
        }
    }

    override fun visitIntegerNumber(ctx: LibSLParser.IntegerNumberContext): IntegerLiteral {
        return IntegerLiteral(ctx.text.toInt())
    }

    override fun visitFloatNumber(ctx: LibSLParser.FloatNumberContext): FloatLiteral {
        return FloatLiteral(ctx.text.toFloat())
    }

    override fun visitQualifiedAccess(ctx: QualifiedAccessContext): QualifiedAccess {
        return when {
            ctx.periodSeparatedFullName() != null -> {
                processPeriodSeparatedQualifiedAccess(ctx.periodSeparatedFullName())
            }

            ctx.simpleCall() != null -> {
                val automatonByFunctionArgumentCreation = visitSimpleCall(ctx.simpleCall())
                val childQualifiedAccess = ctx.qualifiedAccess(0)?.let { visitQualifiedAccess(it) }

                automatonByFunctionArgumentCreation.also {
                    it.childAccess = childQualifiedAccess
                }
            }

            ctx.expressionAtomic() != null -> {
                val parentQualifiedAccess = visitQualifiedAccess(ctx.qualifiedAccess(0))
                val arrayIndex = visitExpressionAtomic(ctx.expressionAtomic())

                val qualifiedArrayAccess = ArrayAccess(arrayIndex)
                val afterArrayQualifiedAccess = ctx.qualifiedAccess(1)?.let { visitQualifiedAccess(it) }
                qualifiedArrayAccess.childAccess = afterArrayQualifiedAccess

                parentQualifiedAccess.also {
                    it.lastChild.childAccess = qualifiedArrayAccess
                }
            }

            else -> error("unknown qualified access kind")
        }
    }

    override fun visitArrayLiteral(ctx: ArrayLiteralContext): Atomic {
        val arrayValues = mutableListOf<Expression>()
        for (value in ctx.expressionsList()?.expression() ?: listOf()) {
            arrayValues.add(visitExpression(value))
        }

        return ArrayLiteral(
            value = arrayValues
        )
    }

    private fun processPeriodSeparatedQualifiedAccess(
        periodSeparatedFullNameContext: PeriodSeparatedFullNameContext
    ): QualifiedAccess {
        val names = periodSeparatedFullNameContext.Identifier().map { it.text.extractIdentifier() }

        val lastAccess = when (val lastFieldName = names.last()) {
            "this" -> ThisAccess(childAccess = null)

            else -> let {
                val lastVariableReference = VariableReferenceBuilder.build(lastFieldName, context)
                VariableAccess(
                    lastFieldName,
                    childAccess = null,
                    lastVariableReference
                )
            }
        }

        return names.dropLast(1).foldRight(lastAccess) { name, access ->
            val childAccess = when (name) {
                "this" -> ThisAccess(childAccess = access)

                else -> {
                    val childVariableReference = VariableReferenceBuilder.build(name, context)
                    VariableAccess(name, childAccess = access, childVariableReference)
                }
            }

            childAccess
        }
    }

    override fun visitSimpleCall(ctx: SimpleCallContext): AutomatonOfFunctionArgumentInvoke {
        check(context is FunctionContext) { "simple call is allowed only inside of function" }

        val automatonName = ctx.Identifier(0).asPeriodSeparatedString()
        val automatonReference = AutomatonReferenceBuilder.build(automatonName, context)

        val argName = ctx.Identifier(1).asPeriodSeparatedString()
        val arg = context.resolveFunctionArgumentByName(argName)

        check(arg != null) { "can't resolve argument $argName" }

        return AutomatonOfFunctionArgumentInvoke(
            automatonReference,
            arg,
            childAccess = null
        )
    }

    override fun visitCallAutomatonConstructorWithNamedArgs(
        ctx: LibSLParser.CallAutomatonConstructorWithNamedArgsContext
    ): Expression {
        val automatonName = ctx.name.asPeriodSeparatedString()
        val automatonRef = AutomatonReferenceBuilder.build(automatonName, context)
        val args = ctx.namedArgs().argPair().mapNotNull { pair ->
            val name = pair.name.text.extractIdentifier()
            val value = when {
                pair.expression() != null -> visitExpression(pair.expression())
                pair.expressionAtomic() != null -> visitExpressionAtomic(pair.expressionAtomic())
                else -> error("unknown kind")
            }

            if (name == "state") {
                return@mapNotNull null
            }

            ArgumentWithValue(name, value)
        }

        val stateName =
            ctx.namedArgs().argPair().firstOrNull { pair -> pair.name.text == "state" }?.expressionAtomic()?.text
        check(stateName != null)

        val stateRef = AutomatonStateReferenceBuilder.build(stateName, automatonRef, context)

        return CallAutomatonConstructor(automatonRef, args, stateRef)
    }

    override fun visitAssignmentRight(ctx: AssignmentRightContext): Expression {
        return when {
            ctx.expression() != null -> visitExpression(ctx.expression())
            ctx.callAutomatonConstructorWithNamedArgs() != null -> {
                visitCallAutomatonConstructorWithNamedArgs(ctx.callAutomatonConstructorWithNamedArgs())
            }
            else -> error("unknown assignment right kind")
        }
    }

    override fun visitActionUsage(ctx: ActionUsageContext): Expression {
        val name = ctx.Identifier().text.extractIdentifier()
        val expressionVisitor = ExpressionVisitor(context)
        val args = ctx.expressionsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }.orEmpty()

        val argTypes = args.map { argument -> context.typeInferrer.getExpressionType(argument).getReference(context) }
        val actionRef = ActionReferenceBuilder.build(name, argTypes, context)

        val actionUsage = ActionUsage(actionRef, args)

        return ActionExpression(actionUsage)
    }

    override fun visitProcUsage(ctx: ProcUsageContext): Expression {
        val name = visitQualifiedAccess(ctx.qualifiedAccess()).lastChild.toString()
        val expressionVisitor = ExpressionVisitor(context)
        val args = ctx.expressionsList()?.expression()?.map { expr ->
            expressionVisitor.visitExpression(expr)
        }.orEmpty()

        val argTypes = args.map { argument -> context.typeInferrer.getExpressionType(argument).getReference(context) }
        val procRef = FunctionReferenceBuilder.build(name, argTypes, context)

        val procCall = ProcedureCall(procRef, args)

        return ProcExpression(procCall)
    }

    override fun visitUnaryOp(ctx: UnaryOpContext): Expression {
        val op = ArithmeticUnaryOp.fromString(ctx.op.text)

        val value = visitExpression(ctx.expression())
        return UnaryOpExpression(op, value)
    }
}

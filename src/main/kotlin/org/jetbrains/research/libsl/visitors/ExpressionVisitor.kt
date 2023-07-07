package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.ActionUsageContext
import org.jetbrains.research.libsl.LibSLParser.ArrayLiteralContext
import org.jetbrains.research.libsl.LibSLParser.ExpressionContext
import org.jetbrains.research.libsl.LibSLParser.PeriodSeparatedFullNameContext
import org.jetbrains.research.libsl.LibSLParser.ProcUsageContext
import org.jetbrains.research.libsl.LibSLParser.QualifiedAccessContext
import org.jetbrains.research.libsl.LibSLParser.SimpleCallContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.AutomatonStateReferenceBuilder
import org.jetbrains.research.libsl.nodes.references.builders.VariableReferenceBuilder
import org.jetbrains.research.libsl.utils.EntityPosition
import org.jetbrains.research.libsl.utils.PositionGetter

class ExpressionVisitor(
    override val context: LslContextBase
) : LibSLParserVisitor<Expression>(context) {
    private val fileName = context.fileName
    private val posGetter = PositionGetter()

    override fun visitExpression(ctx: ExpressionContext): Expression {
        return when {
            ctx.typeOp != null -> {
                processTypeOperationExpression(ctx)
            }

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

            ctx.callAutomatonConstructorWithNamedArgs() != null -> {
                visitCallAutomatonConstructorWithNamedArgs(ctx.callAutomatonConstructorWithNamedArgs())
            }

            ctx.hasAutomatonConcept() != null -> {
                visitHasAutomatonConcept(ctx.hasAutomatonConcept())
            }

            else -> error("unknown expression type")
        }
    }

    private fun processTypeOperationExpression(ctx: ExpressionContext): TypeOperationExpression {
        return TypeOperationExpression(
            ctx.typeOp.text,
            visitExpression(ctx.expression(0)),
            processTypeIdentifier(ctx.typeIdentifier()),
            posGetter.getCtxPosition(fileName, ctx)
        )
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

        return processBinaryExpression(ctx, left, right, op)
    }

    private fun processBinaryExpression(
        ctx: ExpressionContext,
        left: ExpressionContext,
        right: ExpressionContext,
        op: ArithmeticBinaryOps
    ): BinaryOpExpression {
        val leftExpression = visitExpression(left)
        val rightExpression = visitExpression(right)

        return BinaryOpExpression(
            leftExpression,
            rightExpression,
            op,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    private fun processUnaryExpression(ctx: ExpressionContext): UnaryOpExpression {
        val opText = ctx.op.text
        val op = ArithmeticUnaryOp.fromString(opText)
        val expression = visitExpression(ctx.expression(0))

        return UnaryOpExpression(
            op,
            expression,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    private fun processOldValue(ctx: QualifiedAccessContext): OldValue {
        val value = visitQualifiedAccess(ctx)
        return OldValue(
            value,
            posGetter.getCtxPosition(fileName, ctx)
        )
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
                    BoolLiteral(
                        true,
                        posGetter.getCtxPosition(fileName, primitiveLiteralContext)
                    )
                } else {
                    BoolLiteral(
                        false,
                        posGetter.getCtxPosition(fileName, primitiveLiteralContext)
                    )
                }
            }

            primitiveLiteralContext.DoubleQuotedString() != null -> {
                val literal =
                    primitiveLiteralContext.DoubleQuotedString().asPeriodSeparatedString().removeDoubleQuotes()
                StringLiteral(
                    literal,
                    posGetter.getCtxPosition(fileName, primitiveLiteralContext)
                )
            }

            else -> super.visitPrimitiveLiteral(primitiveLiteralContext) as Atomic
        }
    }

    override fun visitIntegerNumber(ctx: LibSLParser.IntegerNumberContext): IntegerLiteral {
        return IntegerLiteral(
            ctx.text.toInt(),
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    override fun visitFloatNumber(ctx: LibSLParser.FloatNumberContext): FloatLiteral {
        return when(ctx.suffix.text) {
            "f" -> Float32Literal(
                ctx.text.toFloat(),
                entityPosition = posGetter.getCtxPosition(fileName, ctx)
            )
            "d" -> Float64Literal(
                ctx.text.toDouble(),
                entityPosition = posGetter.getCtxPosition(fileName, ctx)
            )
            else -> throw IllegalArgumentException("Incorrect float suffix")
        }
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

                val qualifiedArrayAccess = ArrayAccess(
                    arrayIndex,
                    posGetter.getCtxPosition(fileName, ctx)
                )
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
            value = arrayValues,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )
    }

    private fun processPeriodSeparatedQualifiedAccess(
        periodSeparatedFullNameContext: PeriodSeparatedFullNameContext
    ): QualifiedAccess {
        val names = periodSeparatedFullNameContext.Identifier().map { it.text.extractIdentifier() }

        val lastAccess = when(val lastFieldName = names.last()) {

            "this" ->
                ThisAccess(
                    childAccess = null,
                    entityPosition = posGetter.getCtxPosition(fileName, periodSeparatedFullNameContext)
                )

            else -> let {
                val lastVariableReference = VariableReferenceBuilder.build(lastFieldName, context)
                VariableAccess(
                    lastFieldName,
                    childAccess = null,
                    lastVariableReference,
                    entityPosition = posGetter.getCtxPosition(fileName, periodSeparatedFullNameContext)
                )
            }
        }

        return names.dropLast(1).foldRight(lastAccess) { name, access ->
            val childAccess = when(name) {

                "this" -> ThisAccess(
                    childAccess = access,
                    entityPosition = posGetter.getCtxPosition(fileName, periodSeparatedFullNameContext)
                )

                else -> let {
                    val childVariableReference = VariableReferenceBuilder.build(name, context)
                    VariableAccess(
                        name,
                        childAccess = access,
                        childVariableReference,
                        entityPosition = posGetter.getCtxPosition(fileName, periodSeparatedFullNameContext)
                    )
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
            childAccess = null,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
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

            NamedArgumentWithValue(
                name,
                value,
                posGetter.getCtxPosition(fileName, ctx)
            )
        }

        val stateName =
            ctx.namedArgs().argPair().firstOrNull { pair -> pair.name.text == "state" }?.expressionAtomic()?.text
        check(stateName != null)

        val stateRef = AutomatonStateReferenceBuilder.build(stateName, automatonRef, context)

        return CallAutomatonConstructor(
            automatonRef,
            args,
            stateRef,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    override fun visitActionUsage(ctx: ActionUsageContext): Expression  {
        val name = ctx.Identifier().text.extractIdentifier()
        for (c in name) {
            if(c.isLowerCase()) {
                throw IllegalArgumentException("Action names must be in upper case")
            }
        }
        val expressionVisitor = ExpressionVisitor(context)
        val args = mutableListOf<Expression>()
        if(ctx.expressionsList() != null) {
            ctx.expressionsList().expression().forEach { expr -> args.add(expressionVisitor.visitExpression(expr))}
        }

        val action = Action(
            name,
            args,
            posGetter.getCtxPosition(fileName, ctx)
        )

        return ActionExpression(
            action,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    override fun visitProcUsage(ctx: ProcUsageContext): Expression {
        val name = visitQualifiedAccess(ctx.qualifiedAccess()).lastChild.toString()
        val expressionVisitor = ExpressionVisitor(context)
        val args = mutableListOf<Expression>()
        if(ctx.expressionsList() != null) {
            ctx.expressionsList().expression().forEach { expr -> args.add(expressionVisitor.visitExpression(expr))}
        }
        val procedureCall = ProcedureCall(
            name,
            args,
            posGetter.getCtxPosition(fileName, ctx)
        )

        return ProcExpression(
            procedureCall,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    override fun visitUnaryOp(ctx: LibSLParser.UnaryOpContext): Expression {
        val op = when {
            ctx.PLUS() != null -> let {
                ArithmeticUnaryOp.fromString(ctx.PLUS().text)
            }
            ctx.EXCLAMATION() != null -> let {
                ArithmeticUnaryOp.fromString(ctx.EXCLAMATION().text)
            }
            ctx.MINUS() != null -> let {
                ArithmeticUnaryOp.fromString(ctx.MINUS().text)
            }
            ctx.TILDE() != null -> let {
                ArithmeticUnaryOp.fromString(ctx.TILDE().text)
            }
            else -> error("unknown unary op expression")
        }

        val value = visitExpression(ctx.expression())
        return UnaryOpExpression(
            op,
            value,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    override fun visitHasAutomatonConcept(ctx: LibSLParser.HasAutomatonConceptContext): Expression {
        val variable = visitQualifiedAccess(ctx.qualifiedAccess())
        val automatonConceptName = ctx.name.text
        val automatonReference = AutomatonReferenceBuilder.build(automatonConceptName, context)

        return HasAutomatonConcept(
            variable,
            automatonReference,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }
}

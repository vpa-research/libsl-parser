package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.LibSLParser.*
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.references.builders.*
import org.jetbrains.research.libsl.nodes.references.builders.TypeReferenceBuilder.getReference
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
            ctx.bitShiftOp().uLShift() != null -> let {
                "<<<"
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

            primitiveLiteralContext.nullLiteral != null -> {
                NullLiteral(
                    "null",
                    posGetter.getCtxPosition(fileName, primitiveLiteralContext)
                )
            }

            primitiveLiteralContext.DoubleQuotedString() != null -> {
                val literal =
                    primitiveLiteralContext.DoubleQuotedString().asPeriodSeparatedString().removeDoubleQuotes()
                StringLiteral(
                    literal,
                    posGetter.getCtxPosition(fileName, primitiveLiteralContext)
                )
            }

            primitiveLiteralContext.CHARACTER() != null -> {
                val literal = primitiveLiteralContext.CHARACTER().asPeriodSeparatedString().removeQuotes().single()
                CharacterLiteral(
                    literal,
                    posGetter.getCtxPosition(fileName, primitiveLiteralContext)
                )
            }

            else -> super.visitPrimitiveLiteral(primitiveLiteralContext) as Atomic
        }
    }

    override fun visitIntegerNumber(ctx: LibSLParser.IntegerNumberContext): Atomic {
        if(ctx.suffix() == null) {
            return IntegerLiteral(
                ctx.text.toInt(),
                null,
                posGetter.getCtxPosition(context.fileName, ctx))
        } else {
            return when(ctx.suffix().text) {
                "b" -> IntegerLiteral(
                    ctx.text.dropLast(1).toByte(),
                    "b",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                "ub" -> UnsignedInt8Literal(
                    ctx.text.dropLast(2).toUByte(),
                    "ub",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                "s" -> IntegerLiteral(
                    ctx.text.dropLast(1).toShort(),
                    "s",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                "us" -> UnsignedInt16Literal(
                    ctx.text.dropLast(2).toUShort(),
                    "us",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                null -> IntegerLiteral(
                    ctx.text.toInt(),
                    "",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                "u" -> UnsignedInt32Literal(
                    ctx.text.dropLast(1).toUInt(),
                    "u",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                "L" -> IntegerLiteral(
                    ctx.text.dropLast(1).toLong(),
                    "L",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                "uL" -> UnsignedInt64Literal(
                    ctx.text.dropLast(2).toULong(),
                    "uL",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )
                else -> throw IllegalArgumentException("Incorrect integer suffix")
            }
        }
    }


    override fun visitFloatNumber(ctx: LibSLParser.FloatNumberContext): FloatLiteral {
        return if(ctx.suffix() == null) {
            FloatLiteral(
                ctx.text.toDouble(),
                null,
                posGetter.getCtxPosition(context.fileName, ctx)
            )
        } else {
            when(ctx.suffix().text) {
                "f" -> FloatLiteral(
                    ctx.text.toFloat(),
                    "f",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )

                null -> FloatLiteral(
                    ctx.text.toDouble(),
                    "",
                    posGetter.getCtxPosition(context.fileName, ctx)
                )

                else -> throw IllegalArgumentException("Incorrect float suffix")
            }
        }
    }

    override fun visitQualifiedAccess(ctx: QualifiedAccessContext): QualifiedAccess {
        return when {
            ctx.periodSeparatedFullName() != null -> {
                processPeriodSeparatedQualifiedAccess(ctx.periodSeparatedFullName())
            }

            ctx.simpleCall()!= null && ctx.procUsage() == null -> {
                val automatonByFunctionArgumentCreation = visitSimpleCall(ctx.simpleCall())
                val childQualifiedAccess = ctx.qualifiedAccess(0)?.let { visitQualifiedAccess(it) }

                automatonByFunctionArgumentCreation.also {
                    it.childAccess = childQualifiedAccess
                }
            }

            ctx.simpleCall()!= null && ctx.procUsage() != null -> {
                val automatonProcedureCall = visitSimpleCallWithProcedure(ctx)
                val childQualifiedAccess = ctx.qualifiedAccess(0)?.let { visitQualifiedAccess(it)}

                automatonProcedureCall.also {
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
            val childAccess = when (name) {
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

    override fun visitSimpleCall(ctx: SimpleCallContext): AutomatonVariableInvoke {
        // check(context is FunctionContext) { "simple call is allowed only inside of function" }

        val automatonName = ctx.Identifier().asPeriodSeparatedString()
        val automatonReference = AutomatonReferenceBuilder.build(automatonName, context)

        //val argName = ctx.Identifier(1).asPeriodSeparatedString()
        val arg = visitQualifiedAccess(ctx.qualifiedAccess())

        // check(arg != null) { "can't resolve argument $argName" }

        return AutomatonVariableInvoke(
            automatonReference,
            arg,
            childAccess = null,
            entityPosition = posGetter.getCtxPosition(fileName, ctx)
        )
    }

    fun visitSimpleCallWithProcedure(ctx: QualifiedAccessContext): AutomatonProcedureCall {
        // check(context is FunctionContext) { "simple call is allowed only inside of function" }

        val automatonName = ctx.simpleCall().Identifier().asPeriodSeparatedString()
        val automatonReference = AutomatonReferenceBuilder.build(automatonName, context)

        //val argName = ctx.Identifier(1).asPeriodSeparatedString()
        val arg = visitQualifiedAccess(ctx.simpleCall().qualifiedAccess())

        // check(arg != null) { "can't resolve argument $argName" }

        return AutomatonProcedureCall(
            automatonReference,
            arg,
            childAccess = null,
            procExpression = visitProcUsage(ctx.procUsage()) as ProcExpression,
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

        val argTypes = args.map { argument -> context.typeInferrer.getExpressionType(argument).getReference(context) }
        val actionRef = ActionDeclReferenceBuilder.build(name, argTypes, context)

        val actionUsage = ActionUsage(
            actionRef,
            args,
            posGetter.getCtxPosition(fileName, ctx)
        )

        return ActionExpression(
            actionUsage,
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
        //val argTypes = args.map { argument -> context.typeInferrer.getExpressionType(argument).getReference(context) }
        //val procRef = FunctionReferenceBuilder.build(name, argTypes, context)

        val procCall = ProcedureCall(
            //procRef,
            name,
            args,
            posGetter.getCtxPosition(fileName, ctx)
        )

        return ProcExpression(
            procCall,
            posGetter.getCtxPosition(fileName, ctx)
        )
    }

    override fun visitUnaryOp(ctx: UnaryOpContext): Expression {
        val op = ArithmeticUnaryOp.fromString(ctx.op.text)

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

package org.jetbrains.research.libsl.nodes

abstract class ExpressionVisitor<T> {
    open fun visit(node: Expression): T {
        return when (node) {
            is BoolLiteral -> visitBool(node)
            is NullLiteral -> visitNull(node)
            is CallAutomatonConstructor -> visitCallAutomatonConstructor(node)
            is FloatLiteral -> visitFloatNumber(node)
            is IntegerLiteral -> visitIntegerNumber(node)
            is ArrayAccess -> visitArrayAccess(node)
            is AutomatonVariableInvoke -> visitAutomatonGetter(node)
            is AutomatonProcedureCall -> visitAutomatonProcedureCall(node)
            is VariableAccess -> visitVariableAccess(node)
            is StringLiteral -> visitStringValue(node)
            is BinaryOpExpression -> visitBinaryOpExpression(node)
            is OldValue -> visitOldValue(node)
            is UnaryOpExpression -> visitUnaryOpExpression(node)
            is ConstructorArgument -> visitConstructorArgument(node)
            is FunctionArgument -> visitFunctionArgument(node)
            is ResultVariable -> visitResultVariable(node)
            is Variable -> visitVariable(node)
            is ArrayLiteral -> visitArrayLiteral(node)
            is ActionExpression -> visitActionExpression(node)
            is ProcExpression -> visitProcExpression(node)
            is ThisAccess -> visitThisAccess(node)
            is HasAutomatonConcept -> visitHasAutomatonConcept(node)
            is NamedArgumentWithValue -> visitNamedArgumentWithValue(node)
            is TypeOperationExpression -> visitTypeOperationExpression(node)
            is FunctionUsageExpression -> visitFunctionUsageExpression(node)
            is UnsignedInt16Literal -> visitUInt16Number(node)
            is UnsignedInt32Literal -> visitUInt32Number(node)
            is UnsignedInt64Literal -> visitUInt64Number(node)
            is UnsignedInt8Literal -> visitUInt8Number(node)
        }
    }

    abstract fun visitBool(node: BoolLiteral): T

    abstract fun visitCallAutomatonConstructor(node: CallAutomatonConstructor): T

    abstract fun visitFloatNumber(node: FloatLiteral): T

    abstract fun visitIntegerNumber(node: IntegerLiteral): T
    abstract fun visitUInt8Number(node: UnsignedInt8Literal): T
    abstract fun visitUInt16Number(node: UnsignedInt16Literal): T
    abstract fun visitUInt32Number(node: UnsignedInt32Literal): T
    abstract fun visitUInt64Number(node: UnsignedInt64Literal): T

    abstract fun visitArrayAccess(node: ArrayAccess): T

    abstract fun visitAutomatonGetter(node: AutomatonVariableInvoke): T

    abstract fun visitAutomatonProcedureCall(node: AutomatonProcedureCall): T

    abstract fun visitVariableAccess(node: VariableAccess): T

    abstract fun visitStringValue(node: StringLiteral): T

    abstract fun visitBinaryOpExpression(node: BinaryOpExpression): T

    abstract fun visitOldValue(node: OldValue): T

    abstract fun visitUnaryOpExpression(node: UnaryOpExpression): T

    abstract fun visitConstructorArgument(node: ConstructorArgument): T

    abstract fun visitFunctionArgument(node: FunctionArgument): T

    abstract fun visitResultVariable(node: ResultVariable): T

    abstract fun visitVariable(node: Variable): T

    abstract fun visitArrayLiteral(node: ArrayLiteral): T

    abstract fun visitActionExpression(node: ActionExpression): T

    abstract fun visitProcExpression(node: ProcExpression): T

    abstract fun visitThisAccess(node: ThisAccess): T

    abstract fun visitNamedArgumentWithValue(node: NamedArgumentWithValue): T

    abstract fun visitHasAutomatonConcept(node: HasAutomatonConcept): T

    abstract fun visitTypeOperationExpression(node: TypeOperationExpression): T

    abstract fun visitFunctionUsageExpression(node: FunctionUsageExpression): T

    abstract fun visitNull(node: NullLiteral): T
}

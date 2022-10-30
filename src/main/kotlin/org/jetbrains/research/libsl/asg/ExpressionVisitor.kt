package org.jetbrains.research.libsl.asg

abstract class ExpressionVisitor <T> {
    open fun visit(node: Expression): T {
        return when(node) {
            is BoolLiteral -> visitBool(node)
            is CallAutomatonConstructor -> visitCallAutomatonConstructor(node)
            is FloatLiteral -> visitFloatNumber(node)
            is IntegerLiteral -> visitIntegerNumber(node)
            is ArrayAccess -> visitArrayAccess(node)
            is AutomatonGetter -> visitAutomatonGetter(node)
            is VariableAccess -> visitVariableAccess(node)
            is StringLiteral -> visitStringValue(node)
            is BinaryOpExpression -> visitBinaryOpExpression(node)
            is OldValue -> visitOldValue(node)
            is UnaryOpExpression -> visitUnaryOpExpression(node)
            is ConstructorArgument -> visitConstructorArgument(node)
            is FunctionArgument -> visitFunctionArgument(node)
            is ResultVariable -> visitResultVariable(node)
            is Variable -> visitVariable(node)
        }
    }

    abstract fun visitBool(node: BoolLiteral): T

    abstract fun visitCallAutomatonConstructor(node: CallAutomatonConstructor): T

    abstract fun visitFloatNumber(node: FloatLiteral): T

    abstract fun visitIntegerNumber(node: IntegerLiteral): T

    abstract fun visitArrayAccess(node: ArrayAccess): T

    abstract fun visitAutomatonGetter(node: AutomatonGetter): T

    abstract fun visitVariableAccess(node: VariableAccess): T

    abstract fun visitStringValue(node: StringLiteral): T

    abstract fun visitBinaryOpExpression(node: BinaryOpExpression): T

    abstract fun visitOldValue(node: OldValue): T

    abstract fun visitUnaryOpExpression(node: UnaryOpExpression): T

    abstract fun visitConstructorArgument(node: ConstructorArgument): T

    abstract fun visitFunctionArgument(node: FunctionArgument): T

    abstract fun visitResultVariable(node: ResultVariable): T

    abstract fun visitVariable(node: Variable): T
}
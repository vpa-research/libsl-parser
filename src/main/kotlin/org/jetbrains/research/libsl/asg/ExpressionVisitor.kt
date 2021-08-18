package org.jetbrains.research.libsl.asg

abstract class ExpressionVisitor <T> {
    open fun visit(node: Expression): T {
        return when(node) {
            is Bool -> visitBool(node)
            is CallAutomatonConstructor -> visitCallAutomatonConstructor(node)
            is FloatNumber -> visitFloatNumber(node)
            is IntegerNumber -> visitIntegerNumber(node)
            is AccessAlias -> visitAccessAlias(node)
            is ArrayAccess -> visitArrayAccess(node)
            is AutomatonGetter -> visitAutomatonGetter(node)
            is RealTypeAccess -> visitRealTypeAccess(node)
            is VariableAccess -> visitVariableAccess(node)
            is StringValue -> visitStringValue(node)
            is BinaryOpExpression -> visitBinaryOpExpression(node)
            is OldValue -> visitOldValue(node)
            is UnaryOpExpression -> visitUnaryOpExpression(node)
            is AutomatonVariableDeclaration -> visitAutomatonVariableDeclaration(node)
            is ConstructorArgument -> visitConstructorArgument(node)
            is FunctionArgument -> visitFunctionArgument(node)
            is GlobalVariableDeclaration -> visitGlobalVariableDeclaration(node)
            is ResultVariable -> visitResultVariable(node)
        }
    }

    abstract fun visitBool(node: Bool): T
    abstract fun visitCallAutomatonConstructor(node: CallAutomatonConstructor): T
    abstract fun visitFloatNumber(node: FloatNumber): T
    abstract fun visitIntegerNumber(node: IntegerNumber): T
    abstract fun visitAccessAlias(node: AccessAlias): T
    abstract fun visitArrayAccess(node: ArrayAccess): T
    abstract fun visitAutomatonGetter(node: AutomatonGetter): T
    abstract fun visitRealTypeAccess(node: RealTypeAccess): T
    abstract fun visitVariableAccess(node: VariableAccess): T
    abstract fun visitStringValue(node: StringValue): T
    abstract fun visitBinaryOpExpression(node: BinaryOpExpression): T
    abstract fun visitOldValue(node: OldValue): T
    abstract fun visitUnaryOpExpression(node: UnaryOpExpression): T
    abstract fun visitAutomatonVariableDeclaration(node: AutomatonVariableDeclaration): T
    abstract fun visitConstructorArgument(node: ConstructorArgument): T
    abstract fun visitFunctionArgument(node: FunctionArgument): T
    abstract fun visitGlobalVariableDeclaration(node: GlobalVariableDeclaration): T
    abstract fun visitResultVariable(node: ResultVariable): T
}
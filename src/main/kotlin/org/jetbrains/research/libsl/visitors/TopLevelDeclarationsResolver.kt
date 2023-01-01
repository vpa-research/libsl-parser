package org.jetbrains.research.libsl.visitors

import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.context.AutomatonContext
import org.jetbrains.research.libsl.context.FunctionContext
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager

class TopLevelDeclarationsResolver(
    private val basePath: String,
    private val errorManager: ErrorManager,
    context: LslGlobalContext
) : LibSLParserVisitor<Unit>(context)  {
    override fun visitAutomatonDecl(ctx: LibSLParser.AutomatonDeclContext) {
        val automatonContext = AutomatonContext(context)
        AutomatonResolver(basePath, errorManager, automatonContext).visitAutomatonDecl(ctx)
    }

    override fun visitFunctionDecl(ctx: LibSLParser.FunctionDeclContext) {
        val functionContext = FunctionContext(context)
        FunctionVisitor(functionContext, parentAutomaton = null, errorManager).visitFunctionDecl(ctx)
    }

    override fun visitVariableDecl(ctx: LibSLParser.VariableDeclContext) {
        super.visitVariableDecl(ctx)
    }
}

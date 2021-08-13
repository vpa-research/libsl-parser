package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.ParserRuleContext
import org.jetbrains.research.libsl.LibSLParser

fun parseFunctionName(ctx: LibSLParser.FunctionDeclContext): Pair<String?, String> {
    val parent = (ctx.parent as? LibSLParser.AutomatonStatementContext)?.parent as? LibSLParser.AutomatonDeclContext
    val rawName = ctx.name.text

    return when {
        ctx.periodSeparatedFullName() != null && rawName.contains(".") -> {
            val automatonName = rawName.split(".").dropLast(1).joinToString(".")
            val name = rawName.removePrefix("$automatonName.")

            automatonName to name
        }
        parent != null -> {
            val automatonName = parent.name.text

            automatonName to rawName
        }
        else -> {
            null to rawName
        }
    }
}

fun String.removeQuotes(): String = removeSurrounding("\"", "\"")

inline fun <reified T> ParserRuleContext.getChild(): T? {
    return children.firstOrNull { it is T } as T?
}
inline fun <reified T> ParserRuleContext.getChildren(): List<T> {
    return children.filterIsInstance<T>()
}

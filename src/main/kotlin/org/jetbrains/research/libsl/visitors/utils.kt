package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode
import org.jetbrains.research.libsl.LibSLParser

fun parseFunctionName(ctx: LibSLParser.FunctionDeclContext): Pair<String?, String> {
    val parent = (ctx.parent as? LibSLParser.AutomatonStatementContext)?.parent as? LibSLParser.AutomatonDeclContext
    val rawName = ctx.name.processIdentifier()

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

fun String.removeDoubleQuotes(): String = removeSurrounding("\"", "\"")
fun String.removeQuotes(): String = removeSurrounding("'", "'")

fun TerminalNode.processIdentifier(): String = this.text.removeSurrounding("`", "`")
fun Token.processIdentifier(): String = this.text.removeSurrounding("`", "`")

fun LibSLParser.PeriodSeparatedFullNameContext.processIdentifier(): String =
    Identifier().joinToString(separator = ".") { it.processIdentifier() }

inline fun <reified T> ParserRuleContext.getChild(): T? {
    return children.firstOrNull { it is T } as T?
}
inline fun <reified T> ParserRuleContext.getChildren(): List<T> {
    return children.filterIsInstance<T>()
}


inline fun <reified T> RuleContext.getParentOfType(): T? {
    var current: RuleContext? = this
    while (current != null) {
        if (current is T) return current
        current = current.parent
    }

    return null
}

fun parseStringTokenStringSemicolon(str: String, prefix: String): String {
    return str.removeSurrounding(prefix, ";").filter { !it.isWhitespace() }
}
package org.jetbrains.research.libsl.visitors

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.VocabularyImpl
import org.antlr.v4.runtime.tree.TerminalNode
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.errors.Position

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

fun TerminalNode.processIdentifier(): String = this.text.extractIdentifier()
fun Token.processIdentifier(): String = this.text.extractIdentifier()

fun String.extractIdentifier(): String = removeSurrounding("`", "`")

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

fun Token.position(): Position {
    return Position(this.line, this.charPositionInLine)
}

fun ParserRuleContext.position() = start.position()

val keywords = (LibSLParser.VOCABULARY as VocabularyImpl).literalNames.filterNotNull().map { k -> k.removeQuotes() }

fun addBacktickIfNeeded(identifier: String, canBePeriodSeparated: Boolean = false): String {
    val idPattern = Regex("[a-zA-Z_\$][a-zA-Z\\d_\$]*")
    val periodSeparatedIdPattern = Regex("([a-zA-Z_\$][a-zA-Z\\d_\$]*\\.)*[a-zA-Z_\$][a-zA-Z\\d_\$]*")

    return when {
        keywords.contains(identifier) -> {
            "`$identifier`"
        }
        identifier.matches(idPattern) || canBePeriodSeparated && identifier.matches(periodSeparatedIdPattern) -> {
            identifier
        }
        else -> {
            "`$identifier`"
        }
    }
}

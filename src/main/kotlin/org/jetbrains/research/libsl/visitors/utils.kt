package org.jetbrains.research.libsl.visitors

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

fun removeQuotes(string: String): String = string.removeSurrounding("\"", "\"")

val LibSLParser.SemanticTypeContext.semanticTypeName
    get() = this.enumLikeSemanticType()?.semanticTypeName?.text
        ?: this.simpleSemanticType()?.semanticTypeName?.text
        ?: error("unknown type kind")
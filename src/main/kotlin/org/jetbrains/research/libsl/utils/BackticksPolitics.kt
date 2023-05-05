package org.jetbrains.research.libsl.utils

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.visitors.keywords

object BackticksPolitics {
    fun forIdentifier(str: String): String {
        val idPattern = Regex("^[a-zA-Z_\$][a-zA-Z\\d_\$]*\$")

        return when {
            keywords.contains(str) -> {
                "`$str`"
            }
            str.matches(idPattern) -> {
                str
            }
            else -> {
                "`$str`"
            }
        }
    }

    fun forTypeIdentifier(str: String): String {
        val parseResult = getLslParser(str).typeIdentifier()

        return if (parseResult?.exception == null && parseResult.text == str) {
            str
        } else {
            "`$str`"
        }
    }

    fun forPeriodSeparated(str: String): String {
        val parseResult = getLslParser(str).periodSeparatedFullName()

        return if (parseResult?.exception == null && parseResult.text == str) {
            str
        } else {
            "`$str`"
        }
    }

    private fun getLslParser(str: String): LibSLParser {
        val charStream = CharStreams.fromString(str)
        val lexer = LibSLLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        return LibSLParser(tokenStream).apply {
            removeErrorListeners()
        }
    }
}

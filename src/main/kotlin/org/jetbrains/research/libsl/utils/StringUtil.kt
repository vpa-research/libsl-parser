package org.jetbrains.research.libsl.utils

private val PRETTY_CHARS_chars = String(
    charArrayOf(
        '\u0000',
        '\b',
        '\u000C',
        '\n',
        '\r',
        '\t',
        '\\',
        '\'',
        '\"'
    )
)

private val PRETTY_CHARS_strings = arrayOf(
    "\\0",
    "\\b",
    "\\f",
    "\\n",
    "\\r",
    "\\t",
    "\\\\",
    "\\'",
    "\\\""
)

fun getCharRepresentation(literal: String): Char {
    val prettyIndex = PRETTY_CHARS_strings.indexOf(literal)
    if (prettyIndex != -1)
        return PRETTY_CHARS_chars[prettyIndex]
    else if (literal.startsWith("\\u"))
        return Character.toChars(Integer.parseInt(literal.substring(2), 16))[0]
    else if (literal.startsWith("\\"))
        return Character.toChars(Integer.parseInt(literal.substring(1), 8))[0]
    return literal.toCharArray()[0]
}

fun escapeCharStringRepresentation(value: Char): String {
    val prettyIndex = PRETTY_CHARS_chars.indexOf(value)
    if (prettyIndex != -1)
        return PRETTY_CHARS_strings[prettyIndex]
    return String(value.toString().toByteArray(Charsets.UTF_8))
}
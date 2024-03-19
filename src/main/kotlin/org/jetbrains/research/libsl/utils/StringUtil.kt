package org.jetbrains.research.libsl.utils


val CHAR_BASIC_LATIN_END = 0x7F.toChar()
val UNICODE_SYMBOL_SIZE = 6 // length of "\\u1234"

private val DIGITS = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
)

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

private val PRETTY_CHARS_strings: Array<String> = arrayOf(
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
    val output = CharArray(UNICODE_SYMBOL_SIZE)
    val len = escapeSymbol(value, output, 0)
    return String(output, 0, len)
}

fun escapeSymbol(c: Char, buff: CharArray, pos: Int): Int {
    var pos = pos
    val prettyIndex = PRETTY_CHARS_chars.indexOf(c)
    if (prettyIndex != -1) {
        val prettyString: String = PRETTY_CHARS_strings[prettyIndex]
        val prettyLength: Int = prettyString.length
        prettyString.toCharArray(buff, 0, 0, prettyLength)
        return pos + prettyLength
    }
    if (Character.isISOControl(c) || c > CHAR_BASIC_LATIN_END) {
        buff[pos++] = '\\'
        buff[pos++] = 'u'
        buff[pos++] = (c.code shr (4 * 3) and 0xF).toString(16).toCharArray()[0]
        buff[pos++] = (c.code shr (4 * 2) and 0xF).toString(16).toCharArray()[0]
        buff[pos++] = (c.code shr (4 * 1) and 0xF).toString(16).toCharArray()[0]
        buff[pos++] = (c.code shr (4 * 0) and 0xF).toString(16).toCharArray()[0]
        return pos
    }
    buff[pos++] = c
    return pos
}
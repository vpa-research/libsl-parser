package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.type.*

data class IntegerLiteral(

    // "Any" set in order to pass both signed and unsigned integers

    override val value: Any,
    val suffix: String?
) : Atomic()

data class FloatLiteral(
    override val value: Number,
    val suffix: String?
) : Atomic()

data class StringLiteral(
    override val value: String
) : Atomic()

data class BoolLiteral(
    override val value: Boolean
) : Atomic()

package org.jetbrains.research.libsl.nodes.helpers

import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.utils.BackticksPolitics

object ExpressionDumper {
    fun dump(expression: Expression): String = dump(expression, priority = Int.MAX_VALUE)

    private fun dump(expression: Expression, priority: Int): String {
        return when (expression) {
            is BinaryOpExpression -> dumpBinaryExpression(expression, priority)
            is ActionExpression -> dumpActionExpression(expression)
            is ArrayLiteral -> dumpArrayLiteral(expression)
            is BoolLiteral -> dumpLiteral(expression)
            is CallAutomatonConstructor -> dumpCallAutomatonConstructor(expression)
            is FloatLiteral -> dumpFloatLiteral(expression)
            is IntegerLiteral -> dumpLiteral(expression)
            is ArrayAccess -> dumpArrayAccess(expression)
            is AutomatonOfFunctionArgumentInvoke -> dumpAutomatonOfFunctionArgumentInvoke(expression)
            is ThisAccess -> dumpThisAccess(expression)
            is VariableAccess -> dumpVariableAccess(expression)
            is StringLiteral -> dumpStringLiteral(expression)
            is OldValue -> dumpOldValue(expression)
            is ProcExpression -> dumpProcExpression(expression)
            is UnaryOpExpression -> dumpUnaryOpExpression(expression)
            is Variable -> dumpVariable(expression)
            is HasAutomatonConcept -> dumpHasAutomatonConcept(expression)
            is NamedArgumentWithValue -> dumpNamedArgumentWithValue(expression)
            is TypeOperationExpression -> dumpTypeOperationExpression(expression)
        }
    }

    private fun dumpBinaryExpression(expression: BinaryOpExpression, parentPriority: Int): String {
        return buildString {
            val currentPriority = expression.op.priority
            val addBrackets = currentPriority > parentPriority

            if (addBrackets) {
                append("(")
            }

            append(dump(expression.left, currentPriority))
            append(IPrinter.SPACE)
            append(expression.op.string)
            append(IPrinter.SPACE)
            append(dump(expression.right, currentPriority))

            if (addBrackets) {
                append(")")
            }
        }
    }

    private fun dumpActionExpression(expression: ActionExpression): String {
        return buildString {
            append("action ${BackticksPolitics.forIdentifier(expression.action.name)}(")
            if(expression.action.arguments.isNotEmpty()) {
                val args = expression.action.arguments.map { dump(it) }
                append(args.joinToString(separator = ", "))
            }
            append(")")
        }
    }

    private fun dumpArrayLiteral(expression: ArrayLiteral): String {
        return buildString {
            append("[")
            append(
                expression.value.joinToString(separator = ", ", transform = ::dump)
            )
            append("]")
        }
    }

    private fun dumpCallAutomatonConstructor(expression: CallAutomatonConstructor): String {
        return buildString {
            append("new ${BackticksPolitics.forPeriodSeparated(expression.automatonRef.name)}")

            val formattedArgs = buildList {
                add("state = ${BackticksPolitics.forIdentifier(expression.stateRef.name)}")
                for (arg in expression.args) {
                    add(arg.dumpToString())
                }
            }
            append(formattedArgs.joinToString(separator = ", ", prefix = "(", postfix = ")"))
        }
    }

    private fun dumpLiteral(expression: Atomic): String {
        return expression.value.toString()
    }

    private fun dumpFloatLiteral(expression: FloatLiteral): String {
        return "${expression.value}${expression.suffix}"
    }

    private fun dumpStringLiteral(expression: StringLiteral): String {
        return "\"${expression.value}\""
    }

    private fun dumpArrayAccess(expression: ArrayAccess): String {
        return buildString {
            append("[${dump(expression.index)}]")
            if (expression.childAccess != null) {
                if (expression.childAccess is VariableAccess) {
                    append(".")
                }

                append(dump(expression.childAccess!!))
            }
        }
    }

    private fun dumpAutomatonOfFunctionArgumentInvoke(expression: AutomatonOfFunctionArgumentInvoke): String {
        return buildString {
            append(BackticksPolitics.forPeriodSeparated(expression.automatonReference.name))
            append("(")
            append(BackticksPolitics.forIdentifier(expression.arg.name))
            append(")")

            if (expression.childAccess != null) {
                append(".")
                append(dump(expression.childAccess!!))
            }
        }
    }

    private fun dumpThisAccess(expression: ThisAccess): String {
        return buildString {
            append("this.")
            if (expression.childAccess != null) {
                append(dump(expression.childAccess!!))
            }
        }
    }

    private fun dumpVariableAccess(expression: VariableAccess): String {
        return when {
            expression.childAccess != null && expression.childAccess is VariableAccess -> {
                "${BackticksPolitics.forIdentifier(expression.fieldName)}.${dump(expression.childAccess!!)}"
            }
            expression.childAccess != null -> {
                "${BackticksPolitics.forIdentifier(expression.fieldName)}${dump(expression.childAccess!!)}"
            }
            else -> expression.fieldName
        }
    }

    private fun dumpOldValue(expression: OldValue): String {
        return "(${dump(expression.value)})'"
    }

    private fun dumpProcExpression(expression: ProcExpression): String {
        return buildString {
            append("${BackticksPolitics.forIdentifier(expression.procedureCall.name)}(")
            if(expression.procedureCall.arguments.isNotEmpty()) {
                val args = expression.procedureCall.arguments.map { dump(it) }
                append(args.joinToString(separator = ", "))
            }
            append(")")
        }
    }

    private fun dumpUnaryOpExpression(expression: UnaryOpExpression): String {
        return "${expression.op.string}${dump(expression.value)}"
    }

    private fun dumpVariable(expression: Variable): String {
        return expression.name
    }

    private fun dumpHasAutomatonConcept(expression: HasAutomatonConcept): String {
        return "${expression.variable.dumpToString()} has ${expression.automatonReference.name}"
    }

    private fun dumpNamedArgumentWithValue(expression: NamedArgumentWithValue): String {
        return if(expression.name != null) {
            "${expression.name} = ${expression.value.dumpToString()}"
        } else {
            expression.value.dumpToString()
        }
    }

    private fun dumpTypeOperationExpression(expression: TypeOperationExpression): String {
        val left = expression.expression.dumpToString()
        val resolvedType = expression.typeReference.resolveOrError()
        return buildString {
            append("$left ${expression.opName} ${TypeDumper.dumpResolvedType(resolvedType)}")
        }
    }
}

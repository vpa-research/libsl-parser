package org.jetbrains.research.libsl.asg

import com.google.gson.*

val librarySerializer = JsonSerializer<Library> { src, _, _ ->
    JsonObject().apply {
        addProperty("name", src.metadata.name)

        // add meta-information
        if (src.metadata.lslVersion != null) {
            addProperty("lslVersion", src.metadata.stringVersion)
        }
        if (src.metadata.language != null) {
            addProperty("lang", src.metadata.language)
        }
        if (src.metadata.libraryVersion != null) {
            addProperty("library-version", src.metadata.libraryVersion)
        }
        if (src.metadata.url != null) {
            addProperty("url", src.metadata.url)
        }
    }
}

val automatonSerializer = JsonSerializer<Automaton> { src, _, context ->
    JsonObject().apply {
        addProperty("name", src.name)

        add("constructorVariables", JsonArray().apply {
            src.constructorVariables.sortedBy { it.name }.forEach { variable ->
                val variableObject = JsonObject().apply {
                    val variableType = context.serialize(variable.type)
                    addProperty("name", variable.name)
                    add("type", variableType)
                }
                add(variableObject)
            }
        })

        add("variables", JsonArray().apply {
            src.internalVariables.sortedBy { it.name }.forEach { variable ->
                val variableObject = JsonObject().apply {
                    val variableType = context.serialize(variable.type)
                    addProperty("name", variable.name)
                    add("type", variableType)
                }
                add(variableObject)
            }
        })


    }
}

val typeSerializer = JsonSerializer<Type> { src, _, _ ->
    JsonObject().apply {
        addProperty("name", src.semanticType)
        addProperty("realName", src.realType.name.joinToString("."))
        if (src.realType.generic != null) {
            addProperty("realNameGeneric", src.realType.generic!!.joinToString("."))
        }
    }
}

val functionSerializer = JsonSerializer<Function> { src, _, context ->
    JsonObject().apply {
        addProperty("name", src.name)
        addProperty("automaton", src.automatonName)
        addProperty("returnType", src.returnType?.semanticType)

        add("args", JsonArray().apply {
            src.args.forEach { arg ->
                JsonObject().apply {
                    addProperty("name", arg.name)
                    addProperty("type", arg.type.semanticType)
                }
            }
        })

        add("contracts", JsonArray().apply {
            src.contracts.forEach { contract ->
                JsonObject().apply {
                    addProperty("name", contract.name)
                    add("kind", context.serialize(contract.kind))
                    add("expression", context.serialize(contract.expression))
                }
            }
        })

        add("statements", JsonArray().apply {
            src.statements.forEach { statement ->
                add(context.serialize(statement))
            }
        })
    }
}

val expressionSerializer = JsonSerializer<Expression> { src, _, context ->
    JsonObject().apply {
        when(src) {
            is BinaryOpExpression -> {
                addProperty("kind", "binary")
                add("left", context.serialize(src.left))
                add("right", context.serialize(src.right))
            }
            is FloatNumber ->  {
                addProperty("kind", "float")
                addProperty("value", src.value)
            }
            is IntegerNumber -> {
                addProperty("kind", "integer")
                addProperty("value", src.value)
            }
            is StringValue -> {
                addProperty("kind", "string")
                addProperty("value", src.value)
            }
            is UnaryOpExpression -> {
                addProperty("kind", "unary")
                when (src.op) {
                    ArithmeticUnaryOp.MINUS -> {
                        addProperty("unaryOp", "minus")
                    }
                    ArithmeticUnaryOp.INVERSION -> {
                        addProperty("unaryOp", "inversion")
                    }
                }
                add("value", context.serialize(src.value))
            }
            is Variable -> {
                addProperty("kind", "variable")
                addProperty("name", src.name)
                addProperty("type", src.type.semanticType)
                if (src.initValue != null) {
                    add("initValue", context.serialize(src.initValue))
                }
            }
            is VariableAccess -> {
                addProperty("kind", "variableAccess")
                addProperty("name", src.name)
                addProperty("automatonOwnerName", src.automaton.name)
                if (src.arrayIndex != null) {
                    addProperty("arrayIndex", src.arrayIndex)
                }
            }
        }
    }
}

val statementSerializer = JsonSerializer<Statement> { src, _, context ->
    when (src) {
        is Assignment -> JsonObject().apply {
            addProperty("kind", "assignment")
            addProperty("variableName", src.variable.name)
            addProperty("variableAutomaton", src.variable.automaton.name)
            if (src.variable.arrayIndex != null) {
                addProperty("variableIndex", src.variable.arrayIndex)
            }
            add("value", context.serialize(src.value))
        }
    }
}
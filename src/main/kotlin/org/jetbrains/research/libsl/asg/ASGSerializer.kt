package org.jetbrains.research.libsl.asg

import com.google.gson.*

val librarySerializer = JsonSerializer<Library> { src, _, context ->
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

        if (src.imports.isNotEmpty()) {
            add("imports", JsonArray().apply {
                src.imports.forEach { import ->
                    add(import)
                }
            })
        }

        if (src.includes.isNotEmpty()) {
            add("includes", JsonArray().apply {
                src.includes.forEach { include ->
                    add(include)
                }
            })
        }

        if (src.semanticTypes.isNotEmpty()) {
            add("types", JsonArray().apply {
                src.semanticTypes.forEach { type ->
                    add(context.serialize(type, Type::class.java))
                }
            })
        }

        if (src.automata.isNotEmpty()) {
            add("automata", JsonArray().apply {
                src.automata.forEach { automaton ->
                    add(context.serialize(automaton, Automaton::class.java))
                }
            })
        }

        if (src.globalVariables.isNotEmpty()) {
            add("variables", JsonArray().apply {
                src.globalVariables.forEach { (_, variable) ->
                    add(context.serialize(variable, Variable::class.java))
                }
            })
        }
    }
}

val automatonSerializer = JsonSerializer<Automaton> { src, _, context ->
    JsonObject().apply {
        addProperty("name", src.name)

        add("constructorVariables", JsonArray().apply {
            src.constructorVariables.forEach { variable ->
                val variableObject = JsonObject().apply {
                    addProperty("name", variable.name)
                    addProperty("type", variable.type.semanticType)
                }
                add(variableObject)
            }
        })

        add("variables", JsonArray().apply {
            src.internalVariables.forEach { variable ->
                val variableObject = JsonObject().apply {
                    addProperty("name", variable.name)
                    addProperty("type", variable.type.semanticType)
                }
                add(variableObject)
            }
        })

        add("states", JsonArray().apply {
            src.states.forEach { state ->
                val stateObject = JsonObject().apply {
                    addProperty("name", state.name)
                    addProperty("kind", state.kind.name)
                }
                add(stateObject)
            }
        })

        add("shifts", JsonArray().apply {
            src.shifts.forEach { shift ->
                val stateObject = JsonObject().apply {
                    addProperty("from", shift.from.name)
                    addProperty("to", shift.to.name)
                    add("functions", JsonObject().apply { shift.functions.forEach { func ->
                        addProperty("name", func.name)
                        add("args", context.serialize(func.args.map { it.type.semanticType }))
                    } })
                }
                add(stateObject)
            }
        })

        add("functions", JsonArray().apply{
            src.functions.forEach { func ->
                add(context.serialize(func))
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

        if (src is EnumLikeType) {
            add("entities", JsonArray().apply {
                src.entities.forEach { entity ->
                    add(JsonObject().apply {
                        addProperty("name", entity.first)
                        addProperty("value", entity.second)
                    })
                }
            })
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
                add(JsonObject().apply {
                    addProperty("name", contract.name)
                    add("kind", context.serialize(contract.kind))
                    add("expression", context.serialize(contract.expression, Expression::class.java))
                })
            }
        })

        add("statements", JsonArray().apply {
            src.statements.forEach { statement ->
                add(context.serialize(statement, Statement::class.java))
            }
        })
    }
}

val expressionSerializer = JsonSerializer<Expression> { src, _, context ->
    JsonObject().apply {
        when(src) {
            is BinaryOpExpression -> {
                addProperty("kind", "binary")
                add("left", context.serialize(src.left, Expression::class.java))
                add("right", context.serialize(src.right, Expression::class.java))
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
                add("value", context.serialize(src.value, Expression::class.java))
            }
            is Variable -> {
                addProperty("kind", "variable")
                addProperty("name", src.name)
                addProperty("type", src.type.semanticType)
                if (src.initValue != null) {
                    add("initValue", context.serialize(src.initValue, Expression::class.java))
                }
            }
            is VariableAccess -> {
                addProperty("kind", "variableAccess")
                addProperty("name", src.name)
                addProperty("automatonOwnerName", src.automaton?.name)
                if (src.arrayIndex != null) {
                    addProperty("arrayIndex", src.arrayIndex)
                }
            }
            is OldValue -> {
                addProperty("kind", "oldValue")
                add("value", context.serialize(src.value, Expression::class.java))
            }
            is CallAutomatonConstructor -> {
                addProperty("kind", "callAutomatonConstructor")
                addProperty("automatonName", src.automaton.name)
                add("args", JsonArray().apply {
                    src.args.forEach { arg ->
                        add(JsonObject().apply {
                            addProperty("name", arg.variable.name)
                            add("value", context.serialize(arg.init, Expression::class.java))
                        })
                    }
                })
            }
        }
    }
}

val statementSerializer = JsonSerializer<Statement> { src, _, context ->
    when (src) {
        is Assignment -> JsonObject().apply {
            addProperty("kind", "assignment")
            addProperty("variableName", src.variable.name)
            addProperty("variableAutomaton", src.variable.automaton?.name)
            if (src.variable.arrayIndex != null) {
                addProperty("variableIndex", src.variable.arrayIndex)
            }
            add("value", context.serialize(src.value, Expression::class.java))
        }

        is Action -> JsonObject().apply {
            addProperty("kind", "action")
            addProperty("actionName", src.name)
            add("args", JsonArray().apply {
                src.arguments.forEach { arg -> add(context.serialize(arg, Expression::class.java)) }
            })
        }
    }
}
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.*
import org.jetbrains.research.libsl.nodes.Function
import org.jetbrains.research.libsl.type.*
import org.junit.jupiter.api.Assertions
import java.io.File
import java.io.FileNotFoundException

const val baseRoot = "./src/test/"
const val testdataPath = "$baseRoot/testdata/"


fun runJsonTest(testName: String) {
    val library = getLibraryAndErrorManager(testName)
//    checkJsonContent(testName, library, errorManager)
}

fun runLslTest(testName: String) {
    val (library, errorManager, context) = getLibraryAndErrorManager(testName)
    checkLslContent(testName, library, context, errorManager)
}

private fun getLibraryAndErrorManager(testName: String): Triple<Library, ErrorManager, LslContextBase> {
    val (libsl, context) = libslFactory()
    return Triple(libsl.loadFromFileName("$testName.lsl"), libsl.errorManager, context)
}

private fun libslFactory(): Pair<LibSL, LslContextBase> {
    val context = LslGlobalContext()
    context.init()

    val libsl = LibSL(testdataPath + "lsl/", context)
    libsl.errorListener = object : BaseErrorListener() {
        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?
        ) {
            Assertions.fail<RecognitionException>("$line:$charPositionInLine: $msg", e)
        }
    }

    return libsl to context
}

//private fun checkJsonContent(testName: String, library: Library, errorManager: ErrorManager) {
//    val jsonContent = gson.toJson(library)
//
//    val expectedFile = File("$testdataPath/expected/json/$testName.json")
//    if (!expectedFile.exists()) {
//        expectedFile.parentFile.mkdirs()
//        expectedFile.writeText(jsonContent)
//        Assertions.fail<FileNotFoundException>("new file was created: $testName")
//    }
//
//    Assertions.assertEquals(expectedFile.readText(), jsonContent)
//    Assertions.assertTrue(errorManager.errors.isEmpty())
//}

private fun checkLslContent(testName: String, library: Library, context: LslContextBase, errorManager: ErrorManager) {
    val lslContent = removeBlankLines(library.dumpToString())

    checkEverythingIsResolved(library, context)

    val expectedFile = File("$testdataPath/expected/lsl/$testName.lsl")
    if (!expectedFile.exists()) {
        expectedFile.writeText(lslContent)
        Assertions.fail<FileNotFoundException>("new file was created: $testName")
    }

    Assertions.assertEquals(removeBlankLines(expectedFile.readText()), lslContent)
    Assertions.assertTrue(errorManager.errors.isEmpty())
}

private fun checkEverythingIsResolved(library: Library, context: LslContextBase) {
    library.automataReferences.forEach { ref ->
        val automaton = ref.resolveOrError()
        checkAutomatonIsResolved(automaton)
    }

    library.semanticTypesReferences.forEach { ref ->
        val type = ref.resolveOrError()
        checkTypeIsResolved(type)
    }
}

private fun checkAutomatonIsResolved(automaton: Automaton) {
    automaton.typeReference.resolveOrError()
    automaton.constructorVariables.forEach { it.typeReference.resolveOrError() }
    automaton.internalVariables.forEach { it.typeReference.resolveOrError() }

    automaton.functions.forEach { func -> checkFunctionIsResolved(func) }
}

private fun checkFunctionIsResolved(function: Function) {
    checkStatementIsResolved(function, function.statements)

    function.returnType?.resolveOrError()
    function.args.forEach { arg -> arg.typeReference.resolveOrError() }
}

private fun checkStatementIsResolved(function: Function, statements: List<Statement>) {
    for (s in statements) {
        when (s) {
            is ActionUsage -> {s.actionReference.resolveOrError()}
            is ProcedureCall -> {s.procReference.resolveOrError()}
            is VariableDeclaration -> {s.variable.typeReference.resolveOrError()}
            is Assignment -> {
                function.context.typeInferrer.getExpressionType(s.left)
                function.context.typeInferrer.getExpressionType(s.value)
            }
            is ElseStatement -> checkStatementIsResolved(function, s.statements)
            is IfStatement -> {
                checkStatementIsResolved(function, s.ifStatements)
                s.elseStatements?.let {
                    checkStatementIsResolved(function, it.statements)
                }
            }
            is ExpressionStatement -> {
                function.context.typeInferrer.getExpressionType(s.expression)
            }
        }
    }
}

private fun checkTypeIsResolved(type: Type) {
    when (type) {
        is ArrayType -> type.generic.resolveOrError()
        is EnumType -> {}
        is EnumLikeSemanticType -> {}
        is SimpleType -> {}
        is TypeAlias -> type.originalType.resolveOrError()
        is PrimitiveType -> {}
        is RealType -> {}
        is StructuredType -> {
            type.entries.forEach { entryType -> entryType.value.resolveOrError() }
        }
    }
}

private fun removeBlankLines(text: String): String {
    return text.lines().filter { line -> line.isNotBlank() }.joinToString(separator = "\n")
}

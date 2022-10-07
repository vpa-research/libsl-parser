import com.google.gson.GsonBuilder
import org.antlr.v4.runtime.*
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.*
import org.jetbrains.research.libsl.asg.Annotation
import org.jetbrains.research.libsl.asg.Function
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.visitors.ASGBuilder
import org.jetbrains.research.libsl.visitors.Resolver
import org.junit.jupiter.api.Assertions
import java.io.File
import java.io.FileNotFoundException

const val baseRoot = "./src/test/"
const val testdataPath = "$baseRoot/testdata/"


fun runJsonTest(testName: String) {
    val errorManager = ErrorManager()
    val library = getLibrary(testName, errorManager)
    checkJsonContent(testName, library, errorManager)
}

fun runLslTest(testName: String) {
    val errorManager = ErrorManager()
    val library = getLibrary(testName, errorManager)
    checkLslContent(testName, library, errorManager)
}

private fun getLibrary(testName: String, errorManager: ErrorManager): Library {
    val fileContent = getLslFileContent(testName)
    val stream = CharStreams.fromString(fileContent)
    val lexer = LibSLLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val context = LslContext()
    context.init()
    val parser = LibSLParser(tokenStream)
    parser.addErrorListener(object : BaseErrorListener() {
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
    })

    val file = parser.file()
    Resolver(context,"$testdataPath/lsl/", errorManager).visitFile(file)
    return ASGBuilder(context, errorManager).visitFile(file)
}

private fun getLslFileContent(name: String): String {
    val file = File("$testdataPath/lsl/$name.lsl")
    if (!file.exists()) {
        error("can't find file $name")
    }

    return file.readText()
}

private fun checkJsonContent(testName: String, library: Library, errorManager: ErrorManager) {
    val jsonContent = gson.toJson(library)

    val expectedFile = File("$testdataPath/expected/json/$testName.json")
    if (!expectedFile.exists()) {
        expectedFile.writeText(jsonContent)
        Assertions.fail<FileNotFoundException>("new file was created: $testName")
    }

    Assertions.assertEquals(expectedFile.readText(), jsonContent)
    Assertions.assertTrue(errorManager.errors.isEmpty())
}

private fun checkLslContent(testName: String, library: Library, errorManager: ErrorManager) {
    val lslContent = library.dumpToString()

    val expectedFile = File("$testdataPath/expected/lsl/$testName.lsl")
    if (!expectedFile.exists()) {
        expectedFile.writeText(lslContent)
        Assertions.fail<FileNotFoundException>("new file was created: $testName")
    }

    Assertions.assertEquals(expectedFile.readText(), lslContent)
    Assertions.assertTrue(errorManager.errors.isEmpty())
}

private val gson = GsonBuilder()
    .setPrettyPrinting()
    .registerTypeAdapter(Library::class.java, librarySerializer)
    .registerTypeAdapter(Automaton::class.java, automatonSerializer)
    .registerTypeAdapter(Type::class.java, typeSerializer)
    .registerTypeAdapter(FunctionArgument::class.java, functionArgumentsSerializer)
    .registerTypeAdapter(Variable::class.java, variableSerializer)
    .registerTypeAdapter(Annotation::class.java, annotationSerializer)
    .registerTypeAdapter(Function::class.java, functionSerializer)
    .registerTypeAdapter(Expression::class.java, expressionSerializer)
    .registerTypeAdapter(QualifiedAccess::class.java, qualifiedAccessSerializer)
    .registerTypeAdapter(Statement::class.java, statementSerializer)
    .create()

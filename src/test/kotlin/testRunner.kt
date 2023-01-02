import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.context.LslGlobalContext
import org.jetbrains.research.libsl.errors.ErrorManager
import org.jetbrains.research.libsl.nodes.Library
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
    val (library, errorManager) = getLibraryAndErrorManager(testName)
    checkLslContent(testName, library, errorManager)
}

private fun getLibraryAndErrorManager(testName: String): Pair<Library, ErrorManager> {
    val libsl = libslFactory()
    return libsl.loadFromFileName("$testName.lsl") to libsl.errorManager
}

private fun libslFactory(): LibSL {
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

    return libsl
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

private fun checkLslContent(testName: String, library: Library, errorManager: ErrorManager) {
    val lslContent = removeBlankLines(library.dumpToString())

    val expectedFile = File("$testdataPath/expected/lsl/$testName.lsl")
    if (!expectedFile.exists()) {
        expectedFile.writeText(lslContent)
        Assertions.fail<FileNotFoundException>("new file was created: $testName")
    }

    Assertions.assertEquals(removeBlankLines(expectedFile.readText()), lslContent)
    Assertions.assertTrue(errorManager.errors.isEmpty())
}

private fun removeBlankLines(text: String): String {
    return text.lines().filter { line -> line.isNotBlank() }.joinToString(separator = "\n")
}

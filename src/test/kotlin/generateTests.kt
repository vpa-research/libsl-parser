import java.io.File
import java.util.*

fun main() {
    val lslDir = File("$testdataPath/lsl/")
    val lsls = lslDir.listFiles()
        .orEmpty()
        .filter { file -> file.isFile && file.extension == "lsl" }
        .sorted()
        .map { it.nameWithoutExtension }

    val generatedFunctions = mutableListOf<String>()
    for (file in lsls) {
        generatedFunctions.add(generateTestFunction(file, TestType.LSL))
    }

    val resultFile = buildString {
        appendLine("/*")
        appendLine("   DO NOT MODIFY MANUALLY!!!")
        appendLine("*/")
        appendLine()
        appendLine("import org.junit.jupiter.api.Test")
        appendLine()
        appendLine("class GeneratedTests {")
        appendLine(generatedFunctions.joinToString(separator = "\n"))
        appendLine("}")
        appendLine()
    }

    val targetFile = File("$baseRoot/kotlin/GeneratedTests.kt")
    targetFile.writeText(resultFile)
}

private fun generateTestFunction(testName: String, testType: TestType) = buildString {
    val capitalizedTestName = testName.replaceFirstChar { firstChar ->
        if (firstChar.isLowerCase()) firstChar.titlecase(Locale.getDefault()) else firstChar.toString()
    }
    val testFunctionName = "test$capitalizedTestName${testType.text}"
    val runTestFunction = testType.function

    appendLine("    @Test")
    appendLine("    fun $testFunctionName() {")
    appendLine("        $runTestFunction(\"$testName\")")
    appendLine("    }")
}


enum class TestType(val text: String, val function: String) {
    LSL("Lsl", "runLslTest")
}

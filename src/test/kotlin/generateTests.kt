import java.io.File

fun main() {
    val lslDir = File("$testdataPath/lsl/")
    val lsls = lslDir.listFiles()
        .orEmpty()
        .filter  { file -> file.isFile && file.extension == "lsl" }
        .sorted()
        .map { it.nameWithoutExtension }

    val generatedFunctions = mutableListOf<String>()
    for (file in lsls) {
        generatedFunctions.add(generateFunction(file))
    }

    val resultFile = buildString {
        appendLine("/*")
        appendLine("   DO NOT MODIFY MANUALLY!!!")
        appendLine("*/")
        appendLine()
        appendLine("import org.junit.jupiter.api.Test")
        appendLine()
        appendLine("class GeneratedTests {")
        appendLine(generatedFunctions.joinToString(separator = "\n\n"))
        appendLine("}")
        appendLine()
    }

    val targetFile = File("$baseRoot/kotlin/GeneratedTests.kt")
    targetFile.writeText(resultFile)
}

fun generateFunction(name: String) = buildString {
    appendLine("    @Test")
    appendLine("    fun test${name.capitalize()}() {")
    appendLine("        testRunner(\"$name\")")
    appendLine("    }")
}
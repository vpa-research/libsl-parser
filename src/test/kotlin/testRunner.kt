import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.LslContext
import org.jetbrains.research.libsl.visitors.ASGBuilder
import org.jetbrains.research.libsl.visitors.Resolver
import org.junit.jupiter.api.Assertions
import java.io.File
import java.io.FileNotFoundException

const val baseRoot = "./src/test/"
const val testdataPath = "$baseRoot/testdata/"

fun testRunner(name: String) {
    val fileContent = getLslFileAsStream(name)
    val stream = CharStreams.fromString(fileContent)
    val lexer = LibSLLexer(stream)
    val tokenStream = CommonTokenStream(lexer)
    val context = LslContext()
    val parser = LibSLParser(tokenStream)
    val file = parser.file()
    Resolver(context).visitFile(file)
    val library = ASGBuilder(context).visitFile(file)

    val gson = GsonBuilder()
        .setPrettyPrinting()
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes?): Boolean {
                if (f?.name == "parent") {
                    return true
                }
                return false
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                if (clazz == LslContext::class.java) {
                    return true
                }
                return false
            }
        })
        .create()
    val prettyContent = gson.toJson(library)

    val expectedFile = File("$testdataPath/expected/$name.json")
    if (!expectedFile.exists()) {
        expectedFile.writeText(prettyContent)
        Assertions.fail<FileNotFoundException>("new file was created: $name")
    }

    Assertions.assertEquals(expectedFile.readText(), prettyContent)
}

private fun getLslFileAsStream(name: String): String {
    val file = File("$testdataPath/lsl/$name.lsl")
    if (!file.exists()) {
        error("can't find file $name")
    }

    return file.readText()
}

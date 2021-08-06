import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jetbrains.research.libsl.LibSLLexer
import org.jetbrains.research.libsl.LibSLParser
import org.jetbrains.research.libsl.asg.LslContext
import org.jetbrains.research.libsl.asg.MetaNode
import org.jetbrains.research.libsl.visitors.ASGBuilder
import org.jetbrains.research.libsl.visitors.Resolver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Parse {
    @Test
    fun simpleTest() {
        val stream = CharStreams.fromFileName(getLslFileAsStream("testdata/lsl/simple.lsl"))
        val lexer = LibSLLexer(stream)
        val tokenStream = CommonTokenStream(lexer)
        val context = LslContext()
        val parser = LibSLParser(tokenStream)
        val file = parser.file()
        Resolver(context).visitFile(file)
        val library = ASGBuilder(context).visitFile(file)
        Assertions.assertNotNull(library)
        Assertions.assertEquals(
            MetaNode(
                "simple",
                "1.0.0f",
                "java",
                "https://github.com/vldf/",
                Triple(1u, 0u, 0u)
            ),
            library.metadata
        )

        Assertions.assertEquals(
            "another/one/file",
            library.imports.first()
        )

        Assertions.assertEquals(
            "file.to.include",
            library.includes.first()
        )

        Assertions.assertEquals(
            2,
            library.automata.first { it.name == "A" }.functions.size
        )
    }

    private fun getLslFileAsStream(name: String): String {
        return this::class.java.getResource(name)?.path ?: error("file not found: $name")
    }
}
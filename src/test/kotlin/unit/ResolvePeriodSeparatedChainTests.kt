package unit

import org.jetbrains.research.libsl.LibSL
import org.jetbrains.research.libsl.nodes.references.builders.VariableReferenceBuilder
import org.jetbrains.research.libsl.type.ArrayType
import org.jetbrains.research.libsl.utils.QualifiedAccessUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import testdataPath

class ResolvePeriodSeparatedChainTests {
    @Test
    fun `resolve chain, chain is full known, chain is resolved`() {
        val libsl = LibSL(testdataPath + "lsl/")
        val library = libsl.loadFromFileName("qualifiedAccesses.lsl")
        val functionContext = library
            .automataReferences.first { it.name == "A" }.resolveOrError()
            .functions.first { it.name == "foo" }
            .context

        val baseVarRef = VariableReferenceBuilder.build("arg", functionContext)
        val parentType = baseVarRef.resolveOrError().typeReference.resolveOrError()

        val result1 = QualifiedAccessUtils.resolveFieldType(parentType, "field")
        Assertions.assertEquals("Type2", result1?.fullName)
        
        val result2 = QualifiedAccessUtils.resolveFieldType(result1!!, "arrayField")
        Assertions.assertEquals("array<Type3>", result2?.fullName)
        Assertions.assertTrue(result2 is ArrayType)
    }
}

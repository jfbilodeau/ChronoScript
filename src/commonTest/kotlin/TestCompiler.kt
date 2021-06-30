import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.lang.assert
import script.*
import kotlin.test.*

class TestCompiler {
    @Test
    fun testCompileFunctionVariable() {
        val code = "{x = 1}"

        val tokenizer = Tokenizer(code)
        val block = Parser(tokenizer).parseBlock()

        val compiler = Compiler()

        compiler.compileBlock(block)

        val function = compiler.generateUserFunction()

        assertEquals(1, function.variableCount)
    }

    @Test
    fun testCompileTwoStatements() {
        val code = "{1+2;2}"

        val tokenizer = Tokenizer(code)
        val block = Parser(tokenizer).parseBlock()

        val compiler = Compiler()

        compiler.compileBlock(block)

        val function = compiler.generateUserFunction()

        assertEquals(2, block.statements.size)
    }

    @Test
    fun testCompileProgram() {
        val code = "a = 1\nb='test'"

        val tokenizer = Tokenizer(code)

        val statements = Parser(tokenizer).parseProgram()

        val compiler = Compiler()

        compiler.compileProgram(statements)

        val runtime = compiler.generateRuntime()

        runtime.start()

        val value = Value()

        runtime.root.getPropertyByName("a", value)

        assert(value.isNumber)
        assertEquals(1.0, value.number)

        runtime.root.getPropertyByName("b", value)

        assert(value.isObject)
        val obj = value.obj!!
        assertIs<StringObject>(obj)

        assertEquals("test", obj.value)
    }

    @Test
    fun testCompileVariables() = runBlockingNoJs {
        val code = "a = 1\nb=2\nc = a + b"

        val runtime = compileProgram("testCompileVariables")
        runtime.start()

        val value = Value()

        runtime.root.getPropertyByName("c", value)

        assert(value.isNumber)
        assertEquals(3.0, value.number)
    }

    @Test
    fun testCompileObject() = runBlockingNoJs {
        val runtime = compileProgram("testCompileObject")
        runtime.start()

        val value = Value()

        runtime.root.getPropertyByName("c", value)

        assert(value.isObject)
        val obj = value.obj

        assertNotNull(obj)

        obj.getPropertyByName("d", value)
        assertEquals(1.0, value.number)
    }

    @Test
    fun testCompileFunction() = runBlockingNoJs {
        val runtime = compileProgram("testCompileFunction")
        runtime.start()

        val value = Value()

        runtime.root.getPropertyByName("test1", value)

        assert(value.isObject)
        val obj = value.obj

        assertIs<UserFunction>(obj)



        obj.getPropertyByName("d", value)
        assertEquals(1.0, value.number)
    }


}

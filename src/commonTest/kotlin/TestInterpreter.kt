import com.soywiz.korio.file.std.resourcesVfs
import script.*
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals

class TestInterpreter {
    @Test
    fun testCompileAndExecuteSimpleExpression() {
        val code = "return (1+2)*3"

        val tokenizer = Tokenizer(code)
        val statement = Parser(tokenizer).parseProgram()

        val compiler = Compiler()

        compiler.compileProgram(statement)

        val function = compiler.generateUserFunction()

        val runtime = compiler.generateRuntime()

        runtime.interpreter.execute(function)

        val value = Value()

        runtime.interpreter.pop(value)

        assertEquals(value.number, 9.0)
    }

    @Test
    fun testCompileAndExecuteReturnNull() {
        val code = "return null"

        val tokenizer = Tokenizer(code)
        val statement = Parser(tokenizer).parseProgram()

        val compiler = Compiler()

        val runtime = compiler.compileProgram(statement)

        runtime.start()

        val value = Value()

        runtime.interpreter.pop(value)

        assertEquals(value.obj, NullObject)
    }
}
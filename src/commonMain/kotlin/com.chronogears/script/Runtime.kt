package script

import com.soywiz.korio.file.std.resourcesVfs

suspend fun compileProgram(filename: String): Runtime {
    val text = resourcesVfs["${filename}.cgs"].readString()

    val tokenizer = Tokenizer(text, filename)

    val parser = Parser(tokenizer)

    val program = parser.parseProgram()

    val compiler = Compiler()

    return compiler.compileProgram(program)
}

class Runtime(val main: UserFunction) {
    val types = mutableListOf<Type>()
    val dictionary = mutableListOf<StringObject>()
    var numbers = DoubleArray(0)
    val root = UserObject()
    val interpreter = Interpreter(this)

    fun type(name: String): Type? {
        return types.find { it.name == name }
    }

    fun typeIndex(name: String): Int {
        return types.indexOfFirst { it.name == name }
    }

    fun dictionaryEntry(text: String): StringObject {
        var entry = dictionary.find { it.value == text }

        if (entry == null) {
            entry = StringObject(text)

            dictionary.add(entry)
        }

        return entry
    }

    fun dictionaryIndex(text: String): Int {
        var index = dictionary.indexOfFirst { it.value == text }

        if (index == -1) {
            dictionary.add(StringObject(text))

            index = dictionary.lastIndex
        }

        return index
    }

    fun start() {
        interpreter.execute(main)
    }
}
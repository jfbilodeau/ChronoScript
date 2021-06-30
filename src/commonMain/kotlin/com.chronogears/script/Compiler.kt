package script

import com.soywiz.korui.layout.MathEx.max

class SymbolTable(val parent: SymbolTable? = null) {
    val symbols = mutableListOf<String>()

    fun hasSymbol(symbol: String): Boolean {
        var result = symbols.contains(symbol)

        if (!result) {
            result = parent?.hasSymbol(symbol) ?: false
        }

        return result
    }

    fun symbolIndex(symbol: String): Int {
        var index = symbols.indexOf(symbol)

        if (index == -1) {
            index = parent?.symbolIndex(symbol) ?: -1
        }

        if (index == -1) {
            symbols.add(symbol)
            index = symbols.lastIndex
        }

        return index
    }

    val size: Int
        get() {
            return symbols.size + (parent?.symbols?.size ?: 0)
        }
}

class Compiler {
    val dictionary = mutableListOf<StringObject>()
    val numbers = mutableListOf<Double>()
    lateinit var main: UserFunction
    val root = UserObject()
    var code = mutableListOf<Int>()
    var symbols = SymbolTable()
    var symbolCount = 0
    var compilingMain = false

    init {
        root.setPropertyByName("modules", UserObject())
    }

    private fun stringIndex(string: String): Int {
        val index = dictionary.indexOfFirst { it.value == string }

        if (index != -1) {
            return index
        }

        dictionary.add(StringObject(string))

        return dictionary.lastIndex
    }

    private fun numberIndex(number: Double): Int {
        val index = numbers.indexOf(number)

        if (index != -1) {
            return index
        }

        numbers.add(number)

        return numbers.lastIndex
    }

    private fun openBlock() {
        symbols = SymbolTable(symbols)
    }

    private fun closeBlock() {
        symbolCount = max(symbolCount, symbols.size)

        symbols = symbols.parent!!
    }

    fun compileProgram(program: Program): Runtime {
        compilingMain = true
        compileRoot(program)

        main = generateUserFunction()

        return generateRuntime()
    }

    fun compileRoot(program: Program) {
        for (statement in program.statements) {
            compileStatement(statement)
        }

        code.add(OpCodes.Return)
    }

    fun generateRuntime(): Runtime {
        val runtime = Runtime(main)

        runtime.dictionary.addAll(dictionary)
        runtime.numbers = numbers.toDoubleArray()

        return runtime
    }

    fun generateUserFunction(): UserFunction {
        val opcodes = IntArray(code.size)

        for (i in 0..code.lastIndex) {
            opcodes[i] = code[i]
        }

        return UserFunction(opcodes, symbolCount)
    }

    fun compileBlock(block: Block) {
        openBlock()

        for (statement in block.statements) {
            compileStatement(statement)
        }

        closeBlock()
    }

//    fun compileModule(module: Module) {
//        code.clear()
//
//        compilingMain = true
//
//        for (statement in module.statements) {
//            compileStatement(statement)
//        }
//    }

    fun compileStatement(statement: Statement) {
        when (statement) {
            is ReturnStatement -> compileReturnStatement(statement)
            is ExpressionStatement -> compileExpressionStatement(statement)

            else -> error("Unknown statement type: ${statement}", statement)
        }
    }

    private fun compileExpressionStatement(statement: ExpressionStatement) {
        compileExpression(statement.expression)
    }

    private fun compileReturnStatement(statement: ReturnStatement) {
        compileExpression(statement.expression)
        code.add(OpCodes.Return)
    }

    fun compileExpression(expression: Expression) {
        when (expression) {
            is NullExpression -> compileNullExpression(expression)
            is NumberExpression -> compileNumberExpression(expression)
            is StringExpression -> compileStringExpression(expression)
            is BinaryExpression -> compileBinaryExpression(expression)
            is AssignmentExpression -> compileAssignmentExpression(expression)
            is VariableExpression -> compileVariableExpression(expression)
            is ObjectExpression -> compileObjectExpression(expression)

            else -> error("Unsupported expression type: ${expression}", expression)
        }
    }

    private fun compileAssignmentExpression(expression: AssignmentExpression) {
        compileExpression(expression.expression)

        compileLeftValue(expression.leftValue)
    }

    private fun compileVariableExpression(expression: VariableExpression) {
        if (compilingMain) {
            code.add(OpCodes.PushRoot)
            code.add(OpCodes.PushMemberValue)
            code.add(stringIndex(expression.variableName))
        } else {
            TODO("Need to implement variables")
        }
    }

    private fun compileLeftValue(leftValue: LeftValueExpression) {
        when (leftValue) {
            is VariableExpression -> compileVariableLeftValue(leftValue)
            is MemberAccessExpression -> compileMemberAccessLeftValue(leftValue)

            else -> error("Unexpected LValue: ${leftValue}", leftValue)
        }
    }

    private fun compileVariableLeftValue(leftValue: VariableExpression) {
        if (compilingMain) {
            code.add(OpCodes.AssignGlobal)
            code.add(stringIndex(leftValue.variableName))
        } else {
            val index = symbols.symbolIndex(leftValue.variableName)

            code.add(OpCodes.AssignVar)
            code.add(index)
        }
    }

    private fun compileMemberAccessLeftValue(leftValue: MemberAccessExpression) {
        TODO("Not yet implemented")
    }

    private fun compileNullExpression(expression: NullExpression) {
        code.add(OpCodes.PushNull)
    }

    fun compileNumberExpression(expression: NumberExpression) {
        val value = expression.number

        val intValue = value.toInt()

        if (intValue.toDouble() == value) {
            code.add(OpCodes.PushInt)
            code.add(intValue)
        } else {
            val index = numberIndex(value)

            code.add(OpCodes.PushNumber)
            code.add(index)
        }
    }

    fun compileStringExpression(expression: StringExpression) {
        val string = expression.string

        val index = stringIndex(string)

        code.add(OpCodes.PushString)
        code.add(index)
    }

    private fun compileBinaryExpression(expression: BinaryExpression) {
        compileExpression(expression.left)
        compileExpression(expression.right)

        when(expression.operator) {
            Operator.Plus -> code.add(OpCodes.Add)
            Operator.Minus -> code.add(OpCodes.Sub)
            Operator.Star -> code.add(OpCodes.Mul)
            Operator.Slash -> code.add(OpCodes.Div)
            Operator.Percent -> code.add(OpCodes.Mod)

            else -> error("Unhandled operator: ${expression.operator}", expression)
        }
    }

    private fun compileObjectExpression(expression: ObjectExpression) {
        code.add(OpCodes.PushNewObject)

        TODO("WIP")
    }

    fun error(message: String, node: AstNode) {
        throw CompilerException(message, node.filename, node.line, node.col)
    }
}


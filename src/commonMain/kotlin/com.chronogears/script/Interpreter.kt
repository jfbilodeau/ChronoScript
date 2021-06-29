package script

class Interpreter(val runtime: Runtime) {
    val stack = Array<Value>(10000) { Value() }
    var instructionIndex = 0
    var stackIndex = 0

    // Temporary values
    var v1 = Value()
    val v2 = Value()
    val v3 = Value()

    fun push(value: Value) {
        stack[stackIndex++].copyFrom(value)
    }

    fun push(number: Double) {
        stack[stackIndex++].number = number
    }

    fun push(obj: Object) {
        stack[stackIndex++].obj = obj
    }

    fun pop(value: Value) {
        val currentValue = stack[--stackIndex]

        value.copyFrom(currentValue)
    }

    val top
        get() = stack[stackIndex]

    fun execute(function: UserFunction) {
        val opcodes = function.opcodes
        instructionIndex = 0
        val stackBase = stackIndex
        stackIndex += function.variableCount
        var running = true

        while (running) {
            val opcode = opcodes[instructionIndex++]
            when (opcode) {
                OpCodes.Return -> running = false
                OpCodes.ReturnNull -> {
                    push(NullObject)
                    running = false
                }
                OpCodes.PushNull -> push(NullObject)
                OpCodes.PushInt -> push(opcodes[instructionIndex++].toDouble())
                OpCodes.PushNumber -> {
                    val numberId = opcodes[instructionIndex++]
                    push(runtime.numbers[numberId])
                }
                OpCodes.PushString -> {
                    val stringId = opcodes[instructionIndex++]
                    push(runtime.dictionary[stringId])
                }
                OpCodes.PushRoot -> push(runtime.root)
                OpCodes.PushMemberValue -> {
                    pop(v1)

                    if (v1.isNumber) {
                        throw RuntimeException("Cannot access member of number")
                    }

                    val stringIndex = opcodes[instructionIndex++]
                    val string = runtime.dictionary[stringIndex]

                    v1.obj!!.getPropertyByName(string.value, v2)

                    push(v2)
                }
                OpCodes.AssignGlobal -> {
                    val stringId = opcodes[instructionIndex++]
                    val propertyName = runtime.dictionary[stringId]

                    pop(v1)

                    runtime.root.setPropertyByName(propertyName.value, v1)
                }
                OpCodes.Add -> {
                    pop(v1)
                    pop(v2)
                    v3.add(v1, v2)
                    push(v3)
                }
                OpCodes.Sub -> {
                    pop(v1)
                    pop(v2)
                    v3.sub(v1, v2)
                    push(v3)
                }
                OpCodes.Mul -> {
                    pop(v1)
                    pop(v2)
                    v3.mul(v1, v2)
                    push(v3)
                }
                OpCodes.Div -> {
                    pop(v1)
                    pop(v2)
                    v3.div(v1, v2)
                    push(v3)
                }
                OpCodes.Mod -> {
                    pop(v1)
                    pop(v2)
                    v3.mod(v1, v2)
                    push(v3)
                }
                else -> {
                    throw RuntimeException("Unexpected OpCode: ${opcode}")
                }
            }
        }

        stackIndex -= function.variableCount
    }
}
package script

abstract class Function: Object() {
    override fun toNumber() = 0.0
    override fun toString() = "Function"
    override fun getPropertyByName(name: String, value: Value) {
        throw Exception("Function does not have properties")
    }

    override fun setPropertyByName(name: String, value: Value) {
        throw Exception("Function does not have properties")
    }

    override fun getPropertyByIndex(index: Int, value: Value) {
        TODO("Not yet implemented")
    }

    override fun setPropertyByIndex(index: Int, value: Value) {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = TODO("Not yet implemented")

    override fun add(other: Value, result: Value) { TODO("Not implemented") }
    override fun sub(other: Value, result: Value) { TODO("Not implemented") }
    override fun mul(other: Value, result: Value) { TODO("Not implemented") }
    override fun div(other: Value, result: Value) { TODO("Not implemented") }
    override fun mod(other: Value, result: Value) { TODO("Not implemented") }
}

class UserFunction(val opcodes: IntArray, val variableCount: Int): Function() {
    override fun invoke(runtime: Runtime, _this: Object) {
        runtime.interpreter.execute(this)
    }
}


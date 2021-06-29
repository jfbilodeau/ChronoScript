package script

import com.soywiz.korio.lang.substr

object LengthFunction : SystemFunction("length") {
    override fun invoke(runtime: Runtime, self: Object) {
        invokeTyped<StringObject>(runtime, self) {
            it.value.length
        }
    }
}

val StringMethods = listOf(
    LengthFunction
)

class StringObject(val value: String) : SystemObject(StringMethods) {
    override fun toNumber() = try {
        value.toDouble()
    } catch (e: NumberFormatException) {
        0.0
    }

    override fun toString() = value

    override fun getPropertyByName(name: String, value: Value) {
        TODO("Not yet implemented")
    }

    override fun setPropertyByName(name: String, value: Value) {
        TODO("Not yet implemented")
    }

    override fun getPropertyByIndex(index: Int, value: Value) {
        if (index in 0 until size) {
            value.obj = StringObject(this.value.substr(index, 1))
        } else {
            value.setToNull()
        }
    }

    override fun setPropertyByIndex(index: Int, value: Value) {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = value.length

    override fun add(other: Value, result: Value) {
        val otherString = other.toString()

        val newString = value + otherString

        result.obj = StringObject(newString)
    }

    override fun sub(other: Value, result: Value) {
        TODO("Not yet implemented")
    }

    override fun mul(other: Value, result: Value) {
        TODO("Not yet implemented")
    }

    override fun div(other: Value, result: Value) {
        TODO("Not yet implemented")
    }

    override fun mod(other: Value, result: Value) {
        TODO("Not yet implemented")
    }
}
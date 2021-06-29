package script

object NullObject : Object() {
    override fun toNumber(): Double = 0.0
    override fun toString(): String = "null"
    override fun getPropertyByName(name: String, value: Value) {
        value.obj = NullObject
    }

    override fun setPropertyByName(name: String, value: Value) {
        throw Exception("Cannot set property $name of null")
    }

    override fun getPropertyByIndex(index: Int, value: Value) {
        throw Exception("Cannot get property index $index of null")
    }

    override fun setPropertyByIndex(index: Int, value: Value) {
        throw Exception("Cannot get property index $index of null")
    }

    override val size: Int
        get() = 0

    override fun invoke(runtime: Runtime, _this: Object) {
        throw Exception("Cannot invoke NullObject")
    }

    override fun add(other: Value, result: Value) {
        result.obj = this
    }

    override fun sub(other: Value, result: Value) {
        result.obj = this
    }

    override fun mul(other: Value, result: Value) {
        result.obj = this
    }

    override fun div(other: Value, result: Value) {
        result.obj = this
    }

    override fun mod(other: Value, result: Value) {
        result.obj = this
    }
}


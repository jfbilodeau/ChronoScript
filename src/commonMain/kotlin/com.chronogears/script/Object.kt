package script

abstract class Object {
    abstract fun toNumber(): Double
    abstract fun getPropertyByName(name: String, value: Value)
    abstract fun setPropertyByName(name: String, value: Value)
    abstract fun getPropertyByIndex(index: Int, value: Value)
    abstract fun setPropertyByIndex(index: Int, value: Value)
    abstract val size: Int
    abstract fun invoke(runtime: Runtime, _this: Object)

    // Operators
    abstract fun add(other: Value, result: Value)
    abstract fun sub(other: Value, result: Value)
    abstract fun mul(other: Value, result: Value)
    abstract fun div(other: Value, result: Value)
    abstract fun mod(other: Value, result: Value)
}
package script

class Value {
    constructor() {
    }

    constructor(number: Double) {
        this.number = number
    }

    constructor(obj: Object) {
        this.obj = obj
    }

    val isNumber
        get() = obj == null

    val isObject
        get() = obj != null

    var number = 0.0
        get() { return field }
        set(value) {
            field = value
            obj = null
        }
    var obj: Object? = NullObject

    fun setToNull() {
        obj = NullObject
    }

    fun toNumber(): Double {
        if (obj == null) {
            return number
        }

        return obj!!.toNumber()
    }

    override fun toString(): String {
        if (obj == null) {
            return number.toString()
        }

        return obj!!.toString()
    }

    fun copyFrom(other: Value) {
        number = other.number
        obj = other.obj
    }

    fun add(v1: Value, v2: Value) {
        if (v1.obj == null) {
            number = v1.toNumber() + v2.toNumber()
        } else {
            v1.obj!!.add(v2, this)
        }
    }

    fun sub(v1: Value, v2: Value) {
        if (v1.obj == null) {
            number = v1.toNumber() - v2.toNumber()
        } else {
            v1.obj!!.sub(v2, this)
        }
    }

    fun mul(v1: Value, v2: Value) {
        if (v1.obj == null) {
            number = v1.toNumber() * v2.toNumber()
        } else {
            v1.obj!!.mul(v2, this)
        }
    }

    fun div(v1: Value, v2: Value) {
        if (v1.obj == null) {
            number = v1.toNumber() / v2.toNumber()
        } else {
            v1.obj!!.div(v2, this)
        }
    }

    fun mod(v1: Value, v2: Value) {
        if (v1.obj == null) {
            number = v1.toNumber() % v2.toNumber()
        } else {
            v1.obj!!.mod(v2, this)
        }
    }
}
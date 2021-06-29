package script

import com.soywiz.korge.view.ktree.KTreeSerializerExt

class Property(val name: String, val value: Value = Value()) {
    constructor(name: String, obj: Object): this(name, Value(obj))
    constructor(name: String, number: Double): this(name, Value(number))
}

open class UserObject: Object() {
    private val properties = mutableListOf<Property>()

    override fun toNumber(): Double {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "UserObject"
    }

    override fun getPropertyByName(name: String, value: Value) {
        val property = properties.find { it.name == name }

        if (property != null) {
            value.copyFrom(property.value)
        } else {
            value.setToNull()
        }
    }

    override fun setPropertyByName(name: String, value: Value) {
        var property = properties.find { it.name == name }

        if (property == null) {
            property = Property(name)
            property.value.copyFrom(value)
            properties.add(property)
        } else {
            property.value.copyFrom(value)
        }
    }

    override fun getPropertyByIndex(index: Int, value: Value) {
        if (index in 1 until size) {
            value.copyFrom(properties[index].value)
        } else {
            value.setToNull()
        }
    }

    override fun setPropertyByIndex(index: Int, value: Value) {
        if (index in 1 until size) {
            properties[index].value.copyFrom(value)
        }
    }

    override val size: Int
        get() = properties.size

    fun setPropertyByName(name: String, obj: Object) {
        var property = properties.find { it.name == name }

        if (property == null) {
            property = Property(name, obj)
            properties.add(property)
        } else {
            property.value.obj = obj
        }
    }

    override fun invoke(runtime: Runtime, self: Object) {
        throw Exception("Cannot invoke UserObject")
    }

    override fun add(other: Value, result: Value) {
        TODO("Not yet implemented")
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
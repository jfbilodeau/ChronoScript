package script

abstract class SystemFunction(val name: String) : Function() {
    inline fun <reified T>invokeTyped(
        runtime: Runtime,
        self: Object,
        callback: (T) -> Unit
    ) {
        if (self is T) {
            callback(self)
        } else {
            throw Exception("Calling function $name on wrong object")
        }
    }


}
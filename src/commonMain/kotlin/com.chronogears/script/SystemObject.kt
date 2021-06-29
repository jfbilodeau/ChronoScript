package script

abstract class SystemObject(val methods: List<Function>): Object() {
    override fun invoke(runtime: Runtime, self: Object) {
        throw Exception("Cannot invoke SystemObject")
    }
}
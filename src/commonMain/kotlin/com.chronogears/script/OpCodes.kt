package script

object OpCodes {
    const val Return = 1
    const val ReturnNull = 2
    const val PushNull = 3
    const val PushInt = 4
    const val PushNumber = 5
    const val PushString = 6
    const val PushVar = 7
    const val PushRoot = 8
    const val PushMemberValue = 9
    const val PushNewObject = 10
    const val PushMemberName = 11
    const val AssignVar = 12
    const val AssignGlobal = 13
    const val AssignModule = 14
    const val Add = 15
    const val Sub = 16
    const val Mul = 17
    const val Div = 18
    const val Mod = 19
}
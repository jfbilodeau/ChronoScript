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
    const val PushMemberName = 10
    const val AssignVar = 11
    const val AssignGlobal = 12
    const val AssignModule = 13
    const val Add = 14
    const val Sub = 15
    const val Mul = 16
    const val Div = 17
    const val Mod = 18
}
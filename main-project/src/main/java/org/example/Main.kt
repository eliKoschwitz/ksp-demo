package org.example

@Function(erstesArgument = "functionWithoutArgs")
object bla :MyAmazingFunctionTwo<String, String>

@Function(erstesArgument = "functionWithoutArgs")
object bla2 :MyAmazingFunction {
    override val arg1: String?
        get() = TODO("Not yet implemented")
    override val arg2: List<Int?>
        get() = TODO("Not yet implemented")
    override val arg3: List<Map<String, *>>
        get() = TODO("Not yet implemented")
    override val arg4: Int
        get() = TODO("Not yet implemented")
}

fun main() {
//    functionWithoutArgs()
//    functionWithArgs(
//        arg1 = "value",
//        arg2 = listOf(1, 2, null),
//        arg3 = listOf(mapOf("key1" to "value1")),
//        arg4 = 2
//    )
}
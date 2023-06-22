package org.example



//@Function(erstesArgument = "functionWithoutArgs")
interface MyAmazingFunctionTwo<K: Any, V: Any>

//@Function(erstesArgument = "functionWithArgs")
interface MyAmazingFunction {
    val arg1: String?
    val arg2: List<Int?>
    val arg3: List<Map<String, *>>
    val arg4: Int
}
package interpreter

import java.util.Scanner

fun main(args: Array<String>) {
    // sample program that outputs constant
    val program = Ast.Program(setOf(Ast.Var("x"), Ast.Var("y")), arrayOf(
        Ast.BasicBlock(Ast.Label.IntLabel(1), arrayOf(), 
            Ast.Jump.Return(Ast.Expr.Constant(777))
        )
    ))
    // launch it
    runProgramFromConsole(program)
}

fun runProgramFromConsole(program: Ast.Program) {
    val argValues = readLine()!!.split(' ').map(String::toInt)
    val argsMap = program.read.zip(argValues).toMap()
    val result = runProgramWithArgs(program, argsMap)
    println(result)
}

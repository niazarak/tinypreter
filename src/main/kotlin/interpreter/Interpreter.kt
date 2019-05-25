package interpreter

data class VmState(
    val memory: MutableMap<Ast.Var, Int>,
    val blocks: Map<Ast.Label, Ast.BasicBlock>,
    var nextBlock: Ast.BasicBlock?,
    var result: Int?
)

sealed class ExprResult {
    data class BoolExpr(val value: Boolean): ExprResult()
    data class IntExpr(val value: Int): ExprResult()
}

/*
    The entry point of interpreting process
*/
fun runProgramWithArgs(program: Ast.Program, args: Map<Ast.Var, Int>): Int {
    val vm = VmState(
        memory = args.toMutableMap(),
        blocks = program.blocks.map { it.label to it }.toMap(),
        nextBlock = program.blocks[0],
        result = null
    )

    // evaluate first block
    // if there is another jump, then it will be set to 'nextBlock' and executed afterwards
    // the purpose of this is to avoid deepening into stack
    while (vm.nextBlock != null) {
        val nextBlock = vm.nextBlock!!
        vm.nextBlock = null
        runBlock(nextBlock, vm)
    }

    if (vm.result != null) {
        return vm.result!!
    }
    throw RuntimeException("Program did not return anything")   
}

fun runBlock(block: Ast.BasicBlock, vm: VmState) {
    for (assignment in block.assignments) {
        runAssignment(assignment, vm)
    }
    runJump(block.jump, vm)
}

fun runAssignment(assignment: Ast.Assignment, vm: VmState) {
    val result = runExpr(assignment.expr, vm)
    if (result is ExprResult.IntExpr) {
        vm.memory.put(assignment.variable, result.value)
    } else {
        throw RuntimeException("Boolean cannot be written to memory")
    }
}

fun runJump(jump: Ast.Jump, vm: VmState) {
    when (jump) {
        is Ast.Jump.GoTo -> vm.nextBlock = vm.blocks[jump.label]!!
        is Ast.Jump.IfElse -> {
            val condResult = runExpr(jump.condition, vm)

            // condition must be boolean
            if (condResult is ExprResult.BoolExpr) {
                if (condResult.value) {
                    vm.nextBlock = vm.blocks[jump.thenClause.label]!!
                } else {
                    vm.nextBlock = vm.blocks[jump.elseClause.label]!!
                }
            } else {
                throw RuntimeException("Expression in condition must have boolean type")
            }
        }
        is Ast.Jump.Return -> {
            val returnResult = runExpr(jump.expr, vm)
            if (returnResult is ExprResult.IntExpr) {
                vm.result = returnResult.value
            } else {
                throw RuntimeException("Expression in 'return' must have int type")
            }
            vm.nextBlock = null // not necessary, just to make things obvious
        }
    }
}

fun runExpr(expr: Ast.Expr, vm: VmState): ExprResult = when (expr) {
    is Ast.Expr.Constant -> ExprResult.IntExpr(expr.value)
    is Ast.Expr.VarExpr -> ExprResult.IntExpr(vm.memory[expr.variable]!!)
    is Ast.Expr.Application -> runApplication(expr, vm)
}

fun runApplication(app: Ast.Expr.Application, vm: VmState) = when (app.operation.name) {
    "=" -> makeOp(vm, app.arguments, { a, b -> ExprResult.BoolExpr(a == b)})
    "<" -> makeOp(vm, app.arguments, { a, b -> ExprResult.BoolExpr(a < b)})
    "<=" -> makeOp(vm, app.arguments, { a, b -> ExprResult.BoolExpr(a <= b)})
    ">" -> makeOp(vm, app.arguments, { a, b -> ExprResult.BoolExpr(a > b)})
    ">=" -> makeOp(vm, app.arguments, { a, b -> ExprResult.BoolExpr(a >= b)})
    "+" -> makeOp(vm, app.arguments, { a, b -> ExprResult.IntExpr(a + b)})
    "-" -> makeOp(vm, app.arguments, { a, b -> ExprResult.IntExpr(a - b)})
    "*" -> makeOp(vm, app.arguments, { a, b -> ExprResult.IntExpr(a * b)})
    "/" -> makeOp(vm, app.arguments, { a, b -> ExprResult.IntExpr(a / b)})
    else -> throw RuntimeException("Invalid operation")
}

fun makeOp(vm: VmState, args: Array<Ast.Expr>, op: (a: Int, b: Int) -> ExprResult): ExprResult {
    if (args.size != 2) {
        throw RuntimeException("Operation must have exactly 2 arguments")
    }
    val arg1 = runExpr(args[0], vm)
    val arg2 = runExpr(args[1], vm)
    if (arg1 is ExprResult.IntExpr && arg2 is ExprResult.IntExpr) {
        return op(arg1.value, arg2.value)
    }
    throw RuntimeException("Operation arguments must be integers")
}

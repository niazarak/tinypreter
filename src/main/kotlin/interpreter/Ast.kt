package interpreter

// grammar
sealed class Ast {
    data class Program(val read: Set<Var>, val blocks: Array<BasicBlock>) : Ast()
    data class BasicBlock(
        val label: Label,
        val assignments: Array<Assignment>,
        val jump: Jump
    ): Ast()
    data class Assignment(val variable: Var, val expr: Expr): Ast()
    sealed class Jump: Ast() {
        data class GoTo(val label: Label): Jump()
        data class IfElse(
            val condition: Expr,
            val thenClause: GoTo,
            val elseClause: GoTo
        ): Jump()
        data class Return(val expr: Expr): Jump()
    }
    data class Var(val name: String): Ast()
    sealed class Expr: Ast() {
        data class Constant(val value: Int): Expr()
        data class VarExpr(val variable: Var): Expr()
        data class Application(val operation: Op, val arguments: Array<Expr>): Expr()
    }
    sealed class Label: Ast() {
        data class IntLabel(val value: Int): Label()
        data class StrLabel(val value: String): Label()
    }
    data class Op(val name: String): Ast()
}

// helpers
fun num(value: Int) = Ast.Expr.Constant(value)
fun op(operation: String) = Ast.Op(operation)
fun id(name: String) = Ast.Expr.VarExpr(Ast.Var(name))
fun id(variable: Ast.Var) = Ast.Expr.VarExpr(variable)
fun apply(operation: String, arg1: Ast.Expr, arg2: Ast.Expr) = Ast.Expr.Application(op(operation), arrayOf(arg1, arg2))
fun label(name: Int) = Ast.Label.IntLabel(name)
fun goto(name: Int) = Ast.Jump.GoTo(label(name))
fun ifElse(condition: Ast.Expr, thenClause: Ast.Jump.GoTo, elseClause: Ast.Jump.GoTo) = Ast.Jump.IfElse(condition, thenClause, elseClause)
fun assign(variable: Ast.Var, expr: Ast.Expr) = Ast.Assignment(variable, expr)
fun block(label: Ast.Label, assignments: Array<Ast.Assignment>, jump: Ast.Jump) = Ast.BasicBlock(label, assignments, jump)
fun ret(expr: Ast.Expr) = Ast.Jump.Return(expr)

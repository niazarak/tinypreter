package interpreter

import kotlin.test.Test
import kotlin.test.assertEquals
import interpreter.Ast.*

class MainTest {
    @Test
    fun testEuclidAlgorithmComputesGcd() {
        // given
        val x = Ast.Var("x")
        val y = Ast.Var("y")
        val program = Ast.Program(setOf(x, y), 
            arrayOf(
                block(label(1), arrayOf(), 
                    ifElse(apply("=", id(x), id(y)), 
                        goto(7),
                        goto(2)
                    )
                ),
                block(label(2), arrayOf(), 
                    ifElse(apply("<", id(x), id(y)), 
                        goto(5),
                        goto(3)
                    )
                ),
                block(label(3), arrayOf(assign(x, apply("-", id(x), id(y)))), 
                    goto(1)
                ),
                block(label(5), arrayOf(assign(y, apply("-", id(y), id(x)))), 
                    goto(1)
                ),
                block(label(7), arrayOf(), 
                    ret(id(x))
                )
            )
        )

        // when
        val result = runProgramWithArgs(program, mapOf(x to 1071, y to 462))

        // then
        assertEquals(result, 21)
    }
}

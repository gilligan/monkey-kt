package evaluator

import `object`.Bool
import `object`.Error
import `object`.Integer
import `object`.Null
import `object`.Object
import lexer.Lexer
import parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class EvalTest {

  @Test
  fun `test eval integer expression`() {
   val testCases = listOf(
     "5" to Integer(5),
     "10" to Integer(10),
     "-5" to Integer(-5),
     "-10" to Integer(-10),
     "5 + 5 + 5 + 5 - 10" to Integer(10),
     "2 * 2 * 2 * 2 * 2" to Integer(32),
     "-50 + 100 + -50" to Integer(0),
     "5 * 2 + 10" to Integer(20),
     "5 + 2 * 10" to Integer(25),
     "20 + 2 * -10" to Integer(0),
     "50 / 2 * 2 + 10" to Integer(60),
     "2 * (5 + 10)" to Integer(30),
     "3 * 3 * 3 + 10" to Integer(37),
     "3 * (3 * 3) + 10" to Integer(37),
     "(5 + 10 * 2 + 15 / 3) * 2 + -10" to Integer(50))

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input), "evaluating $input failed")
    }
  }

  @Test
  fun `test eval bool expression`() {
    val testCases = listOf(
      "true" to Bool(true),
      "false" to Bool(false),
      "1 < 2" to Bool(true),
      "1 > 2" to Bool(false),
      "1 < 1" to Bool(false),
      "1 > 1" to Bool(false),
      "1 == 1" to Bool(true),
      "1 != 1" to Bool(false),
      "1 == 2" to Bool(false),
      "1 != 2" to Bool(true),
      "true == true" to Bool(true),
      "false == false" to Bool(true),
      "true == false" to Bool(false),
      "true != false" to Bool(true),
      "false != true" to Bool(true),
      "(1 < 2) == true" to Bool(true),
      "(1 < 2) == false" to Bool(false),
      "(1 > 2) == true" to Bool(false),
      "(1 > 2) == false" to Bool(true)
    )

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input), "evaluating $input failed")
    }
  }

  @Test
  fun `test bang operator`() {
    val testCases = listOf(
      "!true" to Bool(false),
      "!false" to Bool(true),
      "!5" to Bool(false),
      "!!true" to Bool(true),
      "!!false" to Bool(false),
      "!!5" to Bool(true)
    )

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input), "evaluating $input failed")
    }
  }

  @Test
  fun `test if else expressions`() {
    val testCases = listOf(
      "if (true) { 10 }" to Integer(10),
      "if (false) { 10 }" to Null,
      "if (1) { 10 }" to Integer(10),
      "if (1 < 2) { 10 }" to Integer(10),
      "if (1 > 2) { 10 }" to Null,
      "if (1 > 2) { 10 } else { 20 }" to Integer(20),
      "if (1 < 2) { 10 } else { 20 }" to Integer(10)
    )

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input), "evaluating $input failed")
    }
  }

  @Test
  fun `test return statements`() {
    val testCases = listOf(
      "return 10;" to Integer(10),
      "return 10; 9;" to Integer(10),
      "return 2 * 5; 9;" to Integer(10),
      "9; return 2 * 5; 9;" to Integer(10),
      """
      if (10 > 1) {
        if (10 > 1) {
          return 10;
        }

        return 1;
      }
      """.trimIndent() to Integer(10)
    )

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input), "evaluating $input failed")
    }
  }

  @Test
  fun `test error handling`() {
    val testCases = listOf(
        "5 + true;" to Error("type mismatch: INTEGER + BOOLEAN"),
        "5 + true; 5;" to Error("type mismatch: INTEGER + BOOLEAN"),
        "-true" to Error("unknown operator: -BOOLEAN"),
        "true + false;" to Error("unknown operator: BOOLEAN + BOOLEAN"),
        "5; true + false; 5" to Error("unknown operator: BOOLEAN + BOOLEAN"),
        "if (10 > 1) { true + false; }" to Error("unknown operator: BOOLEAN + BOOLEAN"),
        """
        if (10 > 1) {
          if (10 > 1) {
            return true + false;
          }

          return 1;
        }
        """.trimIndent() to Error("unknown operator: BOOLEAN + BOOLEAN")
    )

    testCases.forEach { (input, expected) ->
      assertEquals(expected, testEval(input), "evaluating $input failed")
    }
  }

  private fun testEval(input: String): Object {
    val program = Parser(Lexer(input)).parseProgram()
    return eval(program)
  }

}

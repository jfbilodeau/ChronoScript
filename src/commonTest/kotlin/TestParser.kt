import script.*
import kotlin.test.*

class TestParser {
	@Test
	fun testParseNull() {
		val code = "null"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<NullExpression>(result)
	}

	@Test
	fun testParseNumber() {
		val code = "1"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<NumberExpression>(result)
		assertEquals(result.number, 1.0)
	}

	@Test
	fun testParseString() {
		val code = "'abc'"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<StringExpression>(result)
		assertEquals(result.string, "abc")
	}

	@Test
	fun testParseTerm() {
		val code = "1+2"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<BinaryExpression>(result)
		assertEquals(result.operator, Operator.Plus)
		val left = result.left
		assertIs<NumberExpression>(left)
		assertEquals(left.number, 1.0)
		val right = result.right
		assertIs<NumberExpression>(right)
		assertEquals(right.number, 2.0)
	}

	@Test
	fun testParseFactor() {
		val code = "1.0 * 2.0"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<BinaryExpression>(result)
		assertEquals(result.operator, Operator.Star)
		val left = result.left
		assertIs<NumberExpression>(left)
		assertEquals(left.number, 1.0)
		val right = result.right
		assertIs<NumberExpression>(right)
		assertEquals(right.number, 2.0)
	}

	@Test
	fun testParseOpPrecedence() {
		val code = "1.0 + 2.0 * 3.0"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<BinaryExpression>(result)
		assertEquals(result.operator, Operator.Plus)
		val left = result.left
		assertIs<NumberExpression>(left)
		assertEquals(left.number, 1.0)
		val right = result.right
		assertIs<BinaryExpression>(right)
		assertEquals(right.operator, Operator.Star)
	}

	@Test
	fun testParseParentesis() {
		val code = "(1.0 + 2.0) * 3.0"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<BinaryExpression>(result)
		assertEquals(result.operator, Operator.Star)
		val left = result.left
		assertIs<BinaryExpression>(left)
		assertEquals(left.operator, Operator.Plus)
		val right = result.right
		assertIs<NumberExpression>(right)
		assertEquals(right.number, 3.0)
	}

	@Test
	fun testParseMultipleTerms() {
		val code = "1.0 + 2.0 + 3.0 + 4"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<BinaryExpression>(result)
		assertEquals(result.operator, Operator.Plus)
		val left = result.left
		assertIs<BinaryExpression>(left)
		assertIs<BinaryExpression>(left.left)
		assertIs<NumberExpression>(left.right)
		val right = result.right
		assertIs<NumberExpression>(right)
		assertEquals(right.number, 4.0)
	}

	@Test
	fun testParseMultipleFactors() {
		val code = "1.0 * 2.0 * 3.0 * 4"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<BinaryExpression>(result)
		assertEquals(result.operator, Operator.Star)
		val left = result.left
		assertIs<BinaryExpression>(left)
		assertIs<BinaryExpression>(left.left)
		assertIs<NumberExpression>(left.right)
		val right = result.right
		assertIs<NumberExpression>(right)
		assertEquals(right.number, 4.0)
	}

	@Test
	fun testParseUnary() {
		val code = "-1"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<UnaryExpression>(result)
		assertEquals(result.operator, Operator.Minus)
		assertIs<NumberExpression>(result.expression)
	}

	@Test
	fun testAssignment() {
		val code = "a = 1"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseExpression()

		assertIs<AssignmentExpression>(result)
	}

	@Test
	fun testParseModule() {
		val code = "a = 1\nb='test'"

		val tokenizer = Tokenizer(code)

		val result = Parser(tokenizer).parseProgram()

		assertEquals(2, result.statements.size)
	}
}
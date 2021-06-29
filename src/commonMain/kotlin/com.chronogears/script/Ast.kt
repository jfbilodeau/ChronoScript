package script

import com.soywiz.korim.vector.Context2d

abstract class AstNode(
    tokenizer: Tokenizer,
    val filename: String = tokenizer.filename,
    val line: Int = tokenizer.line,
    val col: Int = tokenizer.col
)

class ObjectEntry(val key: String, val value: Expression)

abstract class Expression(tokenizer: Tokenizer) : AstNode(tokenizer)
class NullExpression(tokenizer: Tokenizer) : Expression(tokenizer)
class BinaryExpression(val left: Expression, val operator: Operator, val right: Expression, tokenizer: Tokenizer) :
    Expression(tokenizer)

class UnaryExpression(val operator: Operator, val expression: Expression, tokenizer: Tokenizer) : Expression(tokenizer)
class NumberExpression(val number: Double, tokenizer: Tokenizer) : Expression(tokenizer)
class StringExpression(val string: String, tokenizer: Tokenizer) : Expression(tokenizer)
class IdentifierExpression(val name: String, tokenizer: Tokenizer) : Expression(tokenizer)
class ThisExpression(tokenizer: Tokenizer) : Expression(tokenizer)
class ObjectExpression(tokenizer: Tokenizer) {
    val values = mutableListOf<ObjectEntry>()
}

class ArrayExpression(tokenizer: Tokenizer) {
    val values = mutableListOf<Expression>()
}

class CallExpression(val parameters: CallParameters, tokenizer: Tokenizer) : Expression(tokenizer)
class CallParameters(tokenizer: Tokenizer) {
    val parameters = mutableListOf<Expression>()
}

class AssignmentExpression(val leftValue: LeftValueExpression, val expression: Expression, tokenizer: Tokenizer) :
    Expression(tokenizer)

abstract class LeftValueExpression(tokenizer: Tokenizer) : Expression(tokenizer)
class MemberAccessExpression(val expression: Expression, val memberName: String, tokenizer: Tokenizer) :
    LeftValueExpression(tokenizer)

class VariableExpression(val variableName: String, tokenizer: Tokenizer) : LeftValueExpression(tokenizer)

abstract class Statement(tokenizer: Tokenizer) : AstNode(tokenizer)
class IncludeStatement(val scriptName: String, val alias: String, tokenizer: Tokenizer) : Statement(tokenizer)
class IfStatement(
    val condition: Expression,
    val statement: Statement,
    val elseStatement: Statement?,
    tokenizer: Tokenizer
) : Statement(tokenizer)

class ForStatement(val variable: String, val collection: Expression, val block: Block, tokenizer: Tokenizer) :
    Statement(tokenizer)

class ExpressionStatement(val expression: Expression, tokenizer: Tokenizer) : Statement(tokenizer)
class ReturnStatement(val expression: Expression, tokenizer: Tokenizer) : Statement(tokenizer)
class ReturnNullStatement(tokenizer: Tokenizer) : Statement(tokenizer)

open class Block(tokenizer: Tokenizer) : Statement(tokenizer) {
    val statements = mutableListOf<Statement>()
}

open class Module(val name: String, tokenizer: Tokenizer) : Block(tokenizer)
class Include(val name: String, tokenizer: Tokenizer) : Block(tokenizer)
class Program(name: String, tokenizer: Tokenizer) : Module(name, tokenizer)

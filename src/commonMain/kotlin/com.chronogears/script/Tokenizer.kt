package script

import com.soywiz.korio.util.isLetterDigitOrUnderscore

enum class TokenType {
    Invalid,
    Identifier,
    Keyword,
    String,
    Number,
    Operator,
    Eol, // End of line
    Eof, // End of file
}

enum class Operator(val string: String) {
    Invalid(""),
    Plus("+"),
    Minus("-"),
    Star("*"),
    Slash("/"),
    Percent("%"),
    OpenParenthesis("("),
    CloseParenthesis(")"),
    OpenBracket("["),
    CloseBracket("]"),
    OpenBrace("{"),
    CloseBrace("}"),
    Dot("."),
    Ampersand2("&&"),
    Pipe2("||"),
    Equal("="),
    Equal2("=="),
    SemiColon(";"),
    Colon(":"),
    Comma(",")
}

enum class Keyword(val string: String) {
    For("for"),
    If("if"),
    Else("else"),
    In("in"),
    New("new"),
    Null("null"),
    True("true"),
    False("false"),
    Return("return")
}

private val operatorChars = listOf(
    '!',
    '@',
    '#',
    '$',
    '%',
    '^',
    '&',
    '*',
    '(',
    ')',
    '-',
    '+',
    '=',
    '[',
    ']',
    '{',
    '}',
    ':',
    '<',
    '>',
    ',',
    '.',
    '/',
    '?',
    '|'
)

class Tokenizer(val text: String, val filename: String = "<<>>") {
    private var index = 0

    var line = 1
    var col = 1
    var string = ""
    var number = 0.0
    var operator = Operator.Invalid
    var tokenType = TokenType.Invalid
    private val currentChar: Char
        get() = text[index]

    val eof: Boolean
        get() = index >= text.length

    val isString: Boolean
        get() = tokenType == TokenType.String

    val isNumber: Boolean
        get() = tokenType == TokenType.Number

    init {
        next() // Load first token
    }

    fun isOperator(operator: Operator) = tokenType == TokenType.Operator && this.operator == operator
    fun isIdentifier(identifier: String) = tokenType == TokenType.Identifier && this.string == identifier
    fun isKeyword(keyword: String) = tokenType == TokenType.Keyword && string == keyword

    fun nextIgnoreEol(): TokenType {
        do {
            next()
        } while (tokenType == TokenType.Eol && tokenType != TokenType.Eof)

        return tokenType
    }

    fun next(): TokenType {
        skipWhiteSpace()

        if (eof) {
            return TokenType.Eof
        }

        when {
            currentChar.isLetter() -> parseIdentifier()
            currentChar.isDigit() -> parseNumber()
            currentChar == '\'' -> parseString()
            currentChar == '\n' -> parseEol()
            else -> parseOperator()
        }

        return tokenType
    }

    private fun parseEol() {
        string = "\\n"
        number = 0.0
        tokenType = TokenType.Eol

        // Consume \n
        nextChar()
    }

    fun expectString(): String {
        if (tokenType != TokenType.String) {
            error("Expected string")
        }

        val value = string

        next()

        return value
    }

    fun expectIdentifier(): String {
        if (tokenType != TokenType.Identifier) {
            error("Expected identifier")
        }

        val value = string

        next()

        return value
    }

    fun expectNumber(): Double {
        if (tokenType != TokenType.Number) {
            error("Expected number")
        }

        val number = this.number

        next()

        return number
    }

    private fun parseOperator() {
        string = ""

        do {
            string += currentChar

            nextChar()

            if (string in listOf( ")", "]", "}" ))
                break
        } while (!eof && currentChar in operatorChars)

        operator = Operator.values().firstOrNull() { it.string == string } ?: Operator.Invalid

        if (string == "//") {
            parseSingleLineComment()

            next()
        } else {
            tokenType = TokenType.Operator
        }
    }

    private fun parseSingleLineComment() {
        while(!eof && currentChar != '\n') {
            nextChar()
        }
    }

    private fun parseString() {
        string = ""

        nextChar()

        while (!eof && currentChar != '\'') {
            if (currentChar == '\n') {
                error("End of line reached before closing quote")
            }
            string += currentChar

            nextChar()

            if (eof) {
                // Unterminated string
                tokenType = TokenType.Invalid
                return
            }
        }

        nextChar() // Skip over closing '

        tokenType = TokenType.String
    }

    private fun parseNumber() {
        string = ""
        var isFloat = false
        var valid = true

        do {
            string += currentChar

            if (currentChar == '.') {
                if (isFloat) {
                    // Already found a dot
                    valid = false
                }
                isFloat = true
            }

            nextChar()
        } while (!eof && (currentChar.isDigit() || currentChar == '.'))

        if (valid) {
            try {
                number = string.toDouble()
                if (isFloat) {
                    tokenType = TokenType.Number
                } else {
                    tokenType = TokenType.Number
                }
            } catch (e: NumberFormatException) {
                tokenType = TokenType.Invalid
            }
        } else {
            tokenType = TokenType.Invalid
        }
    }

    private fun parseIdentifier() {
        string = ""

        do {
            string += currentChar

            nextChar()
        } while (!eof && currentChar.isLetterDigitOrUnderscore())

        if (string in Keyword.values().map { it.string }) {
            tokenType = TokenType.Keyword
        } else {
            tokenType = TokenType.Identifier
        }
    }

    private fun nextChar() {
        if (eof) {
//            tokenType = TokenType.Eof
            return
        }

        if (text[index] == '\n') {
            col = 0
            line++
        }

        index++
        col++
    }

    private fun skipWhiteSpace() {
        while (!eof && currentChar in arrayOf('\t', ' ', '\r')) {
            nextChar()
        }
    }

    private fun error(message: String) {
        throw TokenizerException(message, filename, line, col)
    }
}


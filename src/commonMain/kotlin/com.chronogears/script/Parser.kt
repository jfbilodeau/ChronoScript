package script

class Parser(val tokenizer: Tokenizer) {
    fun parseProgram(): Program {
        val program = Program(tokenizer.filename, tokenizer)

        parseModuleStatements(program)

        return program
    }

    private fun parseModuleStatements(module: Module) {
        while (!tokenizer.eof) {
            val statement = when {
                tokenizer.isIdentifier("include") -> {
                    parseIncludeStatement()
                }
                else -> {
                    parseStatement()
                }
            }

            module.statements.add(statement)

            if (!tokenizer.eof && !tokenizer.isOperator(Operator.SemiColon) && tokenizer.tokenType != TokenType.Eol) {
                error("Expected ';' or new line. Found '${tokenizer.string}' instead")
            }

            tokenizer.nextIgnoreEol()
        }
    }

    private fun parseIncludeStatement(): IncludeStatement {
        tokenizer.next()

        val includeName = tokenizer.string

        tokenizer.next()

        val alias = if (tokenizer.isKeyword("as")) {
            tokenizer.next()

            val alias = tokenizer.string

            tokenizer.next()

            alias
        } else {
            ""
        }

        return IncludeStatement(includeName, alias, tokenizer)
    }

    fun parseBlock(): Block {
        val block = Block(tokenizer)

        if (!tokenizer.isOperator(Operator.OpenBrace)) {
            error("Expected '{'")
        }

        tokenizer.next()

        while (true) {
            if (tokenizer.isKeyword("return")) {
                val statement = parseReturnStatement()
                block.statements.add(statement)

                if (tokenizer.isOperator(Operator.CloseBrace)) {
                    error("Expected '}'")
                }

                break
            }

            val statement = parseStatement()

            block.statements.add(statement)

            if (!tokenizer.isOperator(Operator.CloseBrace) && !tokenizer.isOperator(Operator.SemiColon) && tokenizer.tokenType != TokenType.Eol) {
                error("Expected '}', ';' or new line. Found '${tokenizer.string}' instead")
            }

            if (tokenizer.isOperator(Operator.CloseBrace)) {
                break;
            }

            if (tokenizer.eof) {
                error("End of file encountered before closing block '}'")
            }

            tokenizer.nextIgnoreEol()
        }

        return block
    }

    fun parseStatement(): Statement {
        val statement = when {
            tokenizer.isKeyword("if") -> parseIfStatement()
            tokenizer.isKeyword("for") -> parseForStatement()
            tokenizer.isKeyword("return") -> parseReturnStatement()
            else -> parseExpressionStatement()
        }

        return statement
    }

    fun parseIfStatement(): IfStatement {
        tokenizer.next()

        val condition = parseExpression()

        var block = parseBlock()

        var elseStatement: Statement? = null

        if (tokenizer.isKeyword("else")) {
            tokenizer.next()

            if (tokenizer.isKeyword("if")) {
                tokenizer.next()

                elseStatement = parseIfStatement()
            } else {
                elseStatement = parseBlock()
            }
        }

        return IfStatement(condition, block, elseStatement, tokenizer)
    }

    fun parseForStatement(): ForStatement {
        return ForStatement("", NullExpression(tokenizer), Block(tokenizer), tokenizer)
    }

    fun parseReturnStatement(): Statement {
        tokenizer.next()

        return if (tokenizer.isOperator(Operator.CloseBrace)) {
            ReturnNullStatement(tokenizer)
        } else {
            val expression = parseExpression()

            ReturnStatement(expression, tokenizer)
        }
    }

    fun parseExpressionStatement(): ExpressionStatement {
        var expression = parseExpression()

        return ExpressionStatement(expression, tokenizer)
    }

    fun parseExpression(): Expression {
        return parseAssignment()
    }

    fun parseAssignment(): Expression {
        val member = parseNull()

        return if (tokenizer.isOperator(Operator.Equal)) {
            tokenizer.next()

            if (member !is LeftValueExpression) {
                error("Expected lvalue . Got ${member}")
            }

            val expression = parseExpression()

            AssignmentExpression(member, expression, tokenizer)
        } else {
            member
        }
    }

    fun parseNull(): Expression {
        if (tokenizer.isKeyword("null")) {
            return NullExpression(tokenizer)
        }

        return parseTerm()
    }

    fun parseTerm(): Expression {
        var lhs = parseFactor()

        while (tokenizer.string in listOf("+", "-")) {
            val operator = tokenizer.operator

            tokenizer.next()

            val rhs = parseFactor()

            lhs = BinaryExpression(lhs, operator, rhs, tokenizer)
        }

        return lhs
    }

    fun parseFactor(): Expression {
        var lhs = parseUnary()

        while (tokenizer.string in listOf("*", "/", "%", "!")) {
            val operator = tokenizer.operator

            tokenizer.next()

            val rhs = parseUnary()

            lhs = BinaryExpression(lhs, operator, rhs, tokenizer)
        }

        return lhs
    }

    fun parseUnary(): Expression {
        if (tokenizer.isOperator(Operator.Minus) || tokenizer.isOperator(Operator.Plus)) {
            val operator = tokenizer.operator

            tokenizer.next()

            val expression = parseExpression()

            return UnaryExpression(operator, expression, tokenizer)
        } else {
            return parsePrimary()
        }
    }

    fun parsePrimary(): Expression {
        return when {
            tokenizer.isOperator(Operator.Dot) -> parseThis()
            tokenizer.isOperator(Operator.OpenParenthesis) -> parseParenthesis()
            tokenizer.isOperator(Operator.OpenBrace) -> parseObject()
            tokenizer.tokenType == TokenType.String -> parseString()
            tokenizer.tokenType == TokenType.Number -> parseNumber()
            tokenizer.tokenType == TokenType.Identifier -> parseVariable()
            else -> parserError("Unexpected token: ${tokenizer.string} (${tokenizer.tokenType})")
        }
    }

    private fun parseVariable(): Expression {
        var expression = VariableExpression(tokenizer.string, tokenizer)

        tokenizer.next()

        return parseMemberAccess(expression)
    }

    fun parseThis(): Expression {
        val thisExpression = ThisExpression(tokenizer)

        tokenizer.next()

        val memberAccess = parseMemberAccess(thisExpression)

        return memberAccess
    }

    private fun parseObject(): Expression {
        tokenizer.nextIgnoreEol()

        val obj = ObjectExpression(tokenizer)

        while (!tokenizer.isOperator(Operator.CloseBrace)) {
            parseObjectEntry(obj)
        }

        tokenizer.nextIgnoreEol()

        return obj
    }

    private fun parseObjectEntry(obj: ObjectExpression) {
        if (tokenizer.tokenType !in listOf(TokenType.String, TokenType.Identifier)) {
            error("Expected identifier. Found \"${tokenizer.string}\" instead")
        }

        val key = tokenizer.string

        tokenizer.nextIgnoreEol()

        if (!tokenizer.isOperator(Operator.Colon)) {
            error("Colon (:) expected. Found \"${tokenizer.string}\" instead")
        }

        tokenizer.nextIgnoreEol()

        val value = parseExpression()

        if (tokenizer.tokenType != TokenType.Eol && tokenizer.isOperator(Operator.Comma)) {
            error("Expected end of line or comma (,). Found \"${tokenizer.string}\" instead")
        }

        tokenizer.next()

        val entry = ObjectEntry(key, value)

        obj.values.add(entry)
    }

    fun parseMemberAccess(expression: Expression): Expression {
        var result = expression

        while (tokenizer.isOperator(Operator.Dot)) {
            tokenizer.next()

            result = MemberAccessExpression(result, tokenizer.expectIdentifier(), tokenizer)
        }

        return result
    }

    private fun parseParameters(): CallParameters {
        val parameters = CallParameters(tokenizer)

        while (tokenizer.string != ")") {
            val expression = parseExpression()

            parameters.parameters.add(expression)
        }

        tokenizer.next()

        return parameters
    }

    fun parseParenthesis(): Expression {
        tokenizer.next()

        val expression = parseExpression()

        if (tokenizer.string != ")") {
            parserError("Expected ')'")
        }

        tokenizer.next()

        return expression
    }

    fun parseNumber(): Expression {
        val expression = NumberExpression(tokenizer.number, tokenizer)

        tokenizer.next()

        return expression
    }

    fun parseString(): StringExpression {
        val expression = StringExpression(tokenizer.string, tokenizer)

        tokenizer.next()

        return expression
    }

    fun parseIdentifier(): Expression {
        val identifier = tokenizer.string

        tokenizer.next()

        if (tokenizer.string == ".") {
            tokenizer.next()

            return MemberAccessExpression(NullExpression(tokenizer), identifier,tokenizer)
        }

        return NullExpression(tokenizer)
    }

    private fun parserError(message: String): Expression {
        throw ParserException(message, tokenizer.filename, tokenizer.line, tokenizer.col)
    }
}


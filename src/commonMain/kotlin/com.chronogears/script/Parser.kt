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
                expect("';' or new line")
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
            parserError("Expected '{'")
        }

        tokenizer.next()

        while (true) {
            if (tokenizer.isKeyword("return")) {
                val statement = parseReturnStatement()
                block.statements.add(statement)

                if (tokenizer.isOperator(Operator.CloseBrace)) {
                    parserError("Expected '}'")
                }

                break
            }

            val statement = parseStatement()

            block.statements.add(statement)

            if (!tokenizer.isOperator(Operator.CloseBrace) && !tokenizer.isOperator(Operator.SemiColon) && tokenizer.tokenType != TokenType.Eol) {
                parserError("Expected '}', ';' or new line. Found '${tokenizer.string}' instead")
            }

            if (tokenizer.isOperator(Operator.CloseBrace)) {
                break;
            }

            if (tokenizer.eof) {
                parserError("End of file encountered before closing block '}'")
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
                parserError("Expected lvalue . Got ${member}")
            } else {

                val expression = parseExpression()

                AssignmentExpression(member, expression, tokenizer)
            }
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
            tokenizer.tokenType == TokenType.Identifier -> parseFunctionOrVariable()
            else -> parserError("Unexpected token: ${tokenizer.string} (${tokenizer.tokenType})")
        }
    }

    private fun parseFunctionOrVariable(): Expression {
        val identifier = tokenizer.string

        tokenizer.next()

        if (tokenizer.isOperator(Operator.OpenParenthesis)) {
            return parseFunctionDeclarationOrCall(identifier)
        } else {
            return parseVariable(identifier)
        }
    }

    private fun parseFunctionDeclarationOrCall(identifier: String): Expression {
        tokenizer.next()

        val parameters = parseParameters()

//        tokenizer.next()

        return if (tokenizer.isOperator(Operator.OpenBrace)) {
            parseFunctionDeclaration(identifier, parameters)
        } else {
            parseFunctionCall(identifier, parameters)
        }
    }

    private fun parseFunctionDeclaration(identifier: String, parameters: CallParameters): Expression {
        val parameterNames = mutableListOf<String>()

        for (parameter in parameters.parameters) {
            if (parameter !is IdentifierExpression) {
                parserError("Expected parameter name.")
            } else {
                val identifier = parameter.identifier

                parameterNames.add(identifier)
            }
        }
    }

    private fun parseFunctionCall(identifier: String, parameters: CallParameters): Expression {
        TODO("Not yet implemented")
    }

    private fun parseVariable(identifier: String): Expression {
        var expression = VariableExpression(identifier, tokenizer)

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
            parserError("Expected identifier. Found \"${tokenizer.string}\" instead")
        }

        val key = tokenizer.string

        tokenizer.nextIgnoreEol()

        if (!tokenizer.isOperator(Operator.Colon)) {
            parserError("Colon (:) expected. Found \"${tokenizer.string}\" instead")
        }

        tokenizer.nextIgnoreEol()

        val value = parseExpression()

        if (tokenizer.tokenType != TokenType.Eol && tokenizer.isOperator(Operator.Comma)) {
            parserError("Expected end of line or comma (,). Found \"${tokenizer.string}\" instead")
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

        while (!tokenizer.isOperator(Operator.CloseParenthesis)) {
            val expression = parseExpression()

            parameters.parameters.add(expression)

            if (tokenizer.isOperator(Operator.Comma) && tokenizer.isOperator(Operator.CloseParenthesis)) {
                expect("',' or ')'")
            }
        }

        tokenizer.next()

        return parameters
    }

    fun parseParenthesis(): Expression {
        tokenizer.next()

        val expression = parseExpression()

        if (tokenizer.string != ")") {
            expect("')'")
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

    private fun expect(expected: String) {
        val message = "Expected ${expected}. Found \"${tokenizer.string}\" instead"
    }

    private fun parserError(message: String): Expression {
        throw ParserException(message, tokenizer.filename, tokenizer.line, tokenizer.col)
    }
}


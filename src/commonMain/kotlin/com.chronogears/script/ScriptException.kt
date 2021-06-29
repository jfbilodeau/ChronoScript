package script

abstract class ScriptException(message: String, filename: String, val line: Int, val col: Int) : Exception(message)
class TokenizerException(message: String, filename: String, line: Int, col: Int) : ScriptException(message, filename, line, col)
class ParserException(message: String, filename: String, line: Int, col: Int) : ScriptException(message, filename, line, col)
class CompilerException(message: String, filename: String, line: Int, col: Int) : ScriptException(message, filename, line, col)
class RuntimeException(message: String): Exception(message)
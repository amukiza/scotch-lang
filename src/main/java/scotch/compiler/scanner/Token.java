package scotch.compiler.scanner;

import static scotch.util.StringUtil.quote;

import java.util.Objects;
import org.apache.commons.lang.builder.EqualsBuilder;
import scotch.compiler.text.NamedSourcePoint;
import scotch.compiler.text.SourceLocation;

public class Token {

    public static Token token(TokenKind kind, Object value, SourceLocation sourceLocation) {
        return new Token(sourceLocation, kind, value);
    }

    private final TokenKind      kind;
    private final Object         value;
    private final SourceLocation sourceLocation;

    private Token(SourceLocation sourceLocation, TokenKind kind, Object value) {
        this.kind = kind;
        this.value = value;
        this.sourceLocation = sourceLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Token) {
            Token other = (Token) o;
            return new EqualsBuilder()
                .append(kind, other.kind)
                .append(value, other.value)
                .append(sourceLocation, other.sourceLocation)
                .isEquals();
        } else {
            return false;
        }
    }

    public int getColumn() {
        return sourceLocation.getStart().getColumn();
    }

    public NamedSourcePoint getEnd() {
        return sourceLocation.getEnd();
    }

    public TokenKind getKind() {
        return kind;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public NamedSourcePoint getStart() {
        return sourceLocation.getStart();
    }

    public int getStartOffset() {
        return sourceLocation.getStartOffset();
    }

    public int getEndOffset() {
        return sourceLocation.getEndOffset();
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValueAs(Class<T> type) {
        return type.cast(getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, value);
    }

    public boolean is(TokenKind kind) {
        return this.kind == kind;
    }

    @Override
    public String toString() {
        return kind + "(" + quote(value) + ")";
    }

    public Token withKind(TokenKind kind) {
        return new Token(sourceLocation, kind, value);
    }

    public enum TokenKind {
        ARROW,
        EQUALS,
        BOOL,
        CHAR,
        DOUBLE,
        DOUBLE_COLON,
        SEMICOLON,
        EOF,
        ID,
        DEFAULT_OPERATOR,
        IN,
        INT,
        BACKSLASH,
        LET,
        STRING,
        OPEN_PAREN,
        CLOSE_PAREN,
        IF,
        ELSE,
        THEN,
        DOT,
        COMMA,
        NEWLINE,
        OPEN_CURLY,
        CLOSE_CURLY,
        OPEN_SQUARE,
        CLOSE_SQUARE,
        PIPE,
        WHERE,
        MATCH,
        ON,
        DOUBLE_ARROW,
        DO,
        BACKWARDS_ARROW,
        MODULE,
        IMPORT,
        UNDERSCORE,
        LEFT,
        RIGHT,
        INFIX,
        PREFIX,
        DATA,
        CLASS,
        INSTANCE,
    }
}

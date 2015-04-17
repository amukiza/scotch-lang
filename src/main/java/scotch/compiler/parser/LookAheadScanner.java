package scotch.compiler.parser;

import static scotch.compiler.scanner.Token.TokenKind.DOT;
import static scotch.compiler.scanner.Token.TokenKind.EOF;
import static scotch.compiler.scanner.Token.TokenKind.ID;

import java.util.ArrayList;
import java.util.List;
import scotch.compiler.scanner.Scanner;
import scotch.compiler.scanner.Token;
import scotch.compiler.text.NamedSourcePoint;

final class LookAheadScanner {

    private final Scanner          delegate;
    private final List<Token>      tokens;
    private       int              position;
    private       NamedSourcePoint previousPosition;

    public LookAheadScanner(Scanner delegate) {
        this.delegate = delegate;
        this.tokens = new ArrayList<>();
    }

    public NamedSourcePoint getPosition() {
        return peekAt(0).getStart();
    }

    public NamedSourcePoint getPreviousPosition() {
        return previousPosition;
    }

    public Token nextToken() {
        Token token = tokens.get(position);
        buffer();
        if (position < tokens.size()) {
            previousPosition = peekAt(0).getEnd();
            position++;
        }
        return token;
    }

    public Token peekAt(int offset) {
        buffer();
        if (position + offset < tokens.size()) {
            return tokens.get(position + offset);
        } else {
            return tokens.get(tokens.size() - 1);
        }
    }

    private void buffer() {
        if (tokens.isEmpty()) {
            while (true) {
                Token token = delegate.nextToken();
                tokens.add(token);
                if (token.is(EOF)) {
                    break;
                }
            }
            identifyComposes();
        }
    }

    private void identifyComposes() {
        for (int i = 1; i < tokens.size() - 1; i++) {
            Token token = tokens.get(i);
            if (token.is(DOT)) {
                Token previous = tokens.get(i - 1);
                Token next = tokens.get(i + 1);
                if (token.getStartOffset() > previous.getEndOffset() && token.getEndOffset() < next.getStartOffset()) {
                    tokens.set(i, token.withKind(ID));
                }
            }
        }
    }
}

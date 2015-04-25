package scotch.compiler.scanner;

import static java.util.Arrays.asList;
import static scotch.compiler.scanner.LayoutScanner.State.ACCEPT;
import static scotch.compiler.scanner.LayoutScanner.State.SCAN_DEFAULT;
import static scotch.compiler.scanner.LayoutScanner.State.SCAN_DISABLED;
import static scotch.compiler.scanner.LayoutScanner.State.SCAN_LAYOUT;
import static scotch.compiler.scanner.LayoutScanner.State.SCAN_LET;
import static scotch.compiler.scanner.Token.TokenKind.EOF;
import static scotch.compiler.scanner.Token.TokenKind.IN;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_CURLY;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_SQUARE;
import static scotch.compiler.scanner.Token.TokenKind.NEWLINE;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_CURLY;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_SQUARE;
import static scotch.compiler.scanner.Token.TokenKind.SEMICOLON;
import static scotch.compiler.scanner.Token.token;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import scotch.compiler.text.NamedSourcePoint;

public final class LayoutScanner implements Scanner {

    private static final int LOOK_AHEAD = 5;
    private final Scanner        delegate;
    private final Deque<State>   states;
    private final Deque<Integer> braces;
    private final Deque<Integer> indents;
    private final Deque<Integer> letIndents;
    private final List<Token>    tokens;

    public LayoutScanner(Scanner delegate) {
        this.delegate = delegate;
        this.states = new ArrayDeque<>(asList(SCAN_DEFAULT));
        this.braces = new ArrayDeque<>();
        this.indents = new ArrayDeque<>();
        this.letIndents = new ArrayDeque<>();
        this.tokens = new ArrayList<>();
    }

    @Override
    public NamedSourcePoint getPosition() {
        return delegate.getPosition();
    }

    @Override
    public URI getSource() {
        return delegate.getSource();
    }

    @Override
    public Token nextToken() {
        if (tokens.isEmpty()) {
            buffer();
        } else {
            buffer();
            advance();
        }
        layout();
        return firstToken();
    }

    private void accept() {
        enterState(ACCEPT);
    }

    private void advance() {
        tokens.remove(0);
    }

    private void bracesDown() {
        braces.push(braces.pop() - 1);
    }

    private void bracesUp() {
        braces.push(braces.pop() + 1);
    }

    private void buffer() {
        if (tokens.isEmpty() || !lastToken().is(EOF) && tokens.size() < LOOK_AHEAD) {
            buffer_();
        }
    }

    private void buffer_() {
        Token token = delegate.nextToken();
        if (token.is(EOF) && !lastToken().is(SEMICOLON)) {
            tokens.add(token(SEMICOLON, ";", token.getSourceLocation()));
        }
        tokens.add(token);
        buffer();
    }

    private int currentColumn() {
        return firstToken().getColumn();
    }

    private int currentIndent() {
        return indents.peek();
    }

    private int currentLetIndent() {
        return letIndents.peek();
    }

    private State currentState() {
        return states.peek();
    }

    private void dedent() {
        indents.pop();
    }

    private void disableScan() {
        enterState(SCAN_DISABLED);
        braces.push(0);
    }

    private void enterLayout() {
        if (secondToken().is(NEWLINE)) {
            exciseNewLine();
            enterLayout();
        } else if (secondToken().is(OPEN_CURLY)) {
            disableScan();
        } else {
            indent(secondToken().getColumn());
            insertLCurly();
            enterState(SCAN_LAYOUT);
            accept();
        }
    }

    private void enterLet() {
        if (secondToken().is(NEWLINE)) {
            exciseNewLine();
            enterLet();
        } else if (secondToken().is(OPEN_CURLY)) {
            disableScan();
        } else {
            letIndent();
            insertLCurly();
            enterState(SCAN_LET);
            accept();
        }
    }

    private void enterState(State state) {
        states.push(state);
    }

    private void exciseNewLine() {
        if (secondToken().is(NEWLINE)) {
            tokens.remove(1);
        } else {
            throw new IllegalStateException();
        }
    }

    private Token firstToken() {
        return tokens.get(0);
    }

    private boolean hasBraces() {
        return braces.peek() > 0;
    }

    private boolean in(State state) {
        return currentState() == state;
    }

    private void indent(int level) {
        indents.push(level);
    }

    private void indent() {
        indent(firstToken().getColumn());
    }

    private void insertIn() {
        insertToken(token(IN, "in", firstToken().getSourceLocation()));
    }

    private void insertLCurly() {
        tokens.add(1, token(OPEN_CURLY, "{", tokens.get(2).getSourceLocation()));
    }

    private void insertRCurly() {
        insertToken(token(CLOSE_CURLY, "}", firstToken().getSourceLocation()));
    }

    private void insertSemicolon() {
        insertToken(token(SEMICOLON, ";", firstToken().getSourceLocation()));
    }

    private void insertToken(Token token) {
        tokens.add(0, token);
    }

    private Token lastToken() {
        return tokens.get(tokens.size() - 1);
    }

    private void layout() {
        buffer();
        layout_();
    }

    private void layout_() {
        switch (firstToken().getKind()) {
            case EOF:
                if (in(SCAN_LAYOUT)) {
                    leaveLayout();
                }
                return;
            case SEMICOLON:
            case OPEN_SQUARE:
            case CLOSE_SQUARE:
            case OPEN_CURLY:
            case CLOSE_CURLY:
                return;
            case WHERE:
            case ON:
            case DO:
                enterLayout();
                return;
            case LET:
                if (in(SCAN_LET) && currentColumn() == currentLetIndent()) {
                    advance();
                    layout();
                } else {
                    enterLet();
                }
                return;
            case NEWLINE:
                advance();
                layout();
                return;
        }
        switch (currentState()) {
            case ACCEPT:
                leaveState();
                break;
            case SCAN_LAYOUT:
                if (currentColumn() == currentIndent()) {
                    insertSemicolon();
                    accept();
                } else if (currentColumn() < currentIndent()) {
                    leaveLayout();
                }
                break;
            case SCAN_LET:
                if (currentColumn() == currentIndent()) {
                    insertSemicolon();
                    accept();
                } else if (firstToken().is(IN)) {
                    leaveLet();
                } else if (currentColumn() < currentIndent()) {
                    insertIn();
                    leaveLet();
                }
                break;
            case SCAN_DISABLED:
                if (firstToken().is(OPEN_CURLY) || firstToken().is(OPEN_SQUARE)) {
                    bracesUp();
                } else if (firstToken().is(CLOSE_CURLY) || secondToken().is(CLOSE_SQUARE)) {
                    if (hasBraces()) {
                        bracesDown();
                    } else {
                        leaveState();
                    }
                }
                break;
            default:
                if (indents.isEmpty()) {
                    indent();
                } else if (currentColumn() <= currentIndent()) {
                    dedent();
                    insertSemicolon();
                    indent(firstToken().getColumn());
                    accept();
                }
        }
    }

    private void leaveLayout() {
        leaveState();
        if (in(SCAN_LET)) {
            insertRCurly();
            insertSemicolon();
            dedent();
        } else {
            insertSemicolon();
            insertRCurly();
            insertSemicolon();
            dedent();
            accept();
        }
    }

    private void leaveLet() {
        insertRCurly();
        insertSemicolon();
        letDedent();
        leaveState();
        accept();
    }

    private void leaveState() {
        if (states.isEmpty()) {
            throw new IllegalStateException("Cannot leave root state");
        } else {
            states.pop();
        }
    }

    private void letDedent() {
        letIndents.pop();
        dedent();
    }

    private void letIndent() {
        letIndents.push(firstToken().getColumn());
        indent(secondToken().getColumn());
    }

    private Token secondToken() {
        return tokens.get(1);
    }

    enum State {
        ACCEPT,
        SCAN_DEFAULT,
        SCAN_DISABLED,
        SCAN_LET,
        SCAN_LAYOUT
    }
}

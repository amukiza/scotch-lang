package scotch.compiler.scanner;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import scotch.compiler.text.NamedSourcePoint;

public interface Scanner {

    static Scanner forFile(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            try (Stream<String> lines = reader.lines()) {
                return new LayoutScanner(new DefaultScanner(file.toUri(), (lines.collect(joining("\n")) + "\n").toCharArray()));
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception); // TODO
        }
    }

    static Scanner forString(URI source, String... data) {
        return new LayoutScanner(new DefaultScanner(source, (join(lineSeparator(), data) + lineSeparator()).toCharArray()));
    }

    NamedSourcePoint getPosition();

    URI getSource();

    Token nextToken();
}

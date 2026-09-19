package gr.csc.fireresponse.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Reads and validates user input. Every method keeps asking until the answer is valid, so a typing
 * mistake never terminates the application. Only the end of the input stream ends a prompt loop.
 */
public class ConsoleInput {

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final BufferedReader reader;
    private final PrintStream out;

    public ConsoleInput(InputStream in, PrintStream out) {
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.out = out;
    }

    /** Prints a prompt and returns the trimmed line. Throws {@link InputEndedException} at end of input. */
    public String readLine(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        try {
            String line = reader.readLine();
            if (line == null) {
                throw new InputEndedException();
            }
            return line.trim();
        } catch (IOException e) {
            throw new InputEndedException();
        }
    }

    public String readNonBlank(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            out.println("  ! This field cannot be empty.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Fall through to the message below.
            }
            out.println("  ! Please enter a whole number between " + min + " and " + max + ".");
        }
    }

    public double readDouble(String prompt, double min, double max) {
        while (true) {
            String line = readLine(prompt);
            try {
                double value = Double.parseDouble(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Fall through to the message below.
            }
            out.println("  ! Please enter a number between " + min + " and " + max + ".");
        }
    }

    /** Shows a numbered list (1..n) and returns the chosen 0-based index. */
    public int chooseIndex(String title, String[] labels) {
        out.println(title + ":");
        for (int i = 0; i < labels.length; i++) {
            out.println("  " + (i + 1) + ". " + labels[i]);
        }
        return readInt("Choose 1-" + labels.length, 1, labels.length) - 1;
    }

    public boolean confirm(String prompt) {
        while (true) {
            String line = readLine(prompt + " (y/n)").toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            out.println("  ! Please answer y or n.");
        }
    }

    /** An empty answer means "use the default"; otherwise the answer must match yyyy-MM-dd HH:mm. */
    public LocalDateTime readDateTimeOrDefault(String prompt, LocalDateTime defaultValue) {
        while (true) {
            String line = readLine(prompt);
            if (line.isEmpty()) {
                return defaultValue;
            }
            try {
                return LocalDateTime.parse(line, DATE_TIME_FORMAT);
            } catch (DateTimeParseException e) {
                out.println("  ! Use the format yyyy-MM-dd HH:mm, for example 2026-08-14 15:30.");
            }
        }
    }
}

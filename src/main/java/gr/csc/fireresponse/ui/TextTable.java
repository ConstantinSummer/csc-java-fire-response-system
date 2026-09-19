package gr.csc.fireresponse.ui;

/**
 * Draws a plain-ASCII table. Rows are stored in a fixed-size array, so the table has a capacity
 * like every other collection in this project. Only ASCII characters are used, which keeps the
 * output readable in every console, including the Windows default code pages.
 */
public class TextTable {

    private final String[] headers;
    private final boolean[] rightAligned;
    private final String[][] rows;
    private int rowCount;

    public TextTable(String[] headers, int maxRows) {
        this.headers = headers;
        this.rightAligned = new boolean[headers.length];
        this.rows = new String[maxRows][];
    }

    /** Marks columns (0-based) whose cells are right-aligned, typically numbers. */
    public TextTable alignRight(int... columns) {
        for (int i = 0; i < columns.length; i++) {
            rightAligned[columns[i]] = true;
        }
        return this;
    }

    public void addRow(String... cells) {
        if (cells.length != headers.length) {
            throw new IllegalArgumentException("Expected " + headers.length + " cells but got " + cells.length);
        }
        if (rowCount == rows.length) {
            throw new IllegalStateException("Table is full (capacity " + rows.length + ")");
        }
        rows[rowCount] = cells;
        rowCount++;
    }

    public String render() {
        int[] widths = new int[headers.length];
        for (int c = 0; c < headers.length; c++) {
            widths[c] = headers[c].length();
            for (int r = 0; r < rowCount; r++) {
                widths[c] = Math.max(widths[c], rows[r][c].length());
            }
        }

        StringBuilder text = new StringBuilder();
        String separator = separator(widths);
        text.append(separator).append('\n');
        text.append(line(headers, widths, false)).append('\n');
        text.append(separator).append('\n');
        for (int r = 0; r < rowCount; r++) {
            text.append(line(rows[r], widths, true)).append('\n');
        }
        text.append(separator);
        return text.toString();
    }

    private String separator(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int c = 0; c < widths.length; c++) {
            line.append(repeat('-', widths[c] + 2)).append('+');
        }
        return line.toString();
    }

    private String line(String[] cells, int[] widths, boolean honourAlignment) {
        StringBuilder line = new StringBuilder("|");
        for (int c = 0; c < cells.length; c++) {
            String padding = repeat(' ', widths[c] - cells[c].length());
            if (honourAlignment && rightAligned[c]) {
                line.append(' ').append(padding).append(cells[c]).append(" |");
            } else {
                line.append(' ').append(cells[c]).append(padding).append(" |");
            }
        }
        return line.toString();
    }

    static String repeat(char symbol, int times) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < times; i++) {
            text.append(symbol);
        }
        return text.toString();
    }
}

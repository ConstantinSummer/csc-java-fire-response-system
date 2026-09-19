package gr.csc.fireresponse.ui;

/** Thrown when the input stream ends (for example Ctrl+D / Ctrl+Z, or the end of a piped script). */
public class InputEndedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputEndedException() {
        super("Input ended");
    }
}

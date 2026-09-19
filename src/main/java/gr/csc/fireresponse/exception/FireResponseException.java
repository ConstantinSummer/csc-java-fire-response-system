package gr.csc.fireresponse.exception;

/**
 * Base class for business-rule violations in the fire response domain.
 *
 * <p>This is a <em>checked</em> exception: callers must handle it or declare it, so a rule
 * violation can never be ignored by accident. Invalid constructor arguments, by contrast,
 * use the unchecked {@link IllegalArgumentException}.</p>
 */
public class FireResponseException extends Exception {

    private static final long serialVersionUID = 1L;

    public FireResponseException(String message) {
        super(message);
    }
}

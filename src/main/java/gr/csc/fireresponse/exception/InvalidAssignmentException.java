package gr.csc.fireresponse.exception;

/** Thrown when a resource cannot be assigned to an incident (unavailable, unsuitable, closed incident). */
public class InvalidAssignmentException extends FireResponseException {

    private static final long serialVersionUID = 1L;

    public InvalidAssignmentException(String message) {
        super(message);
    }
}

package gr.csc.fireresponse.exception;

/** Thrown when a resource is asked to change availability in a way its current state does not allow. */
public class InvalidResourceStateException extends FireResponseException {

    private static final long serialVersionUID = 1L;

    public InvalidResourceStateException(String message) {
        super(message);
    }
}

package gr.csc.fireresponse.exception;

/** Thrown when an incident or resource with the requested id does not exist. */
public class NotFoundException extends FireResponseException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String what, int id) {
        super(what + " with id " + id + " was not found");
    }
}

package gr.csc.fireresponse.exception;

/** Thrown when a fixed-size array-backed structure is already full. */
public class CapacityExceededException extends FireResponseException {

    private static final long serialVersionUID = 1L;

    public CapacityExceededException(String what, int capacity) {
        super(what + " is full (capacity " + capacity + ")");
    }
}

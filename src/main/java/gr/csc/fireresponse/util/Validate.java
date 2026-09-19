package gr.csc.fireresponse.util;

/**
 * Small validation helpers used by constructors in the domain model.
 *
 * <p>These methods are {@code static} on purpose: they are stateless, pure checks that do
 * not belong to any single object. Every failure throws {@link IllegalArgumentException},
 * which signals a programming or input error rather than a business-rule violation.</p>
 */
public final class Validate {

    private Validate() {
        // Utility class: not meant to be instantiated.
    }

    public static void notNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    public static String notBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public static int inRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + " but was " + value);
        }
        return value;
    }

    public static double inRange(double value, double min, double max, String fieldName) {
        if (Double.isNaN(value) || value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max + " but was " + value);
        }
        return value;
    }
}

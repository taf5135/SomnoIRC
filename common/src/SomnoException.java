/**
 * Defines the special exceptions that SomnoClient and SomnoServer use in order
 * to communicate properly.
 */

public class SomnoException extends Exception {
    public SomnoException(String msg) {
        super(msg); //currently just throws a regular exception, need to change this
    }
}

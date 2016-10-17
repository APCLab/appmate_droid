package nctu.fintech.appmate;

/**
 * An exception occur when a malformed resource indicator is assigned.
 */
public class MalformedResourceException extends IllegalArgumentException {

    /**
     * Create an exception instance represent a malformed resource indicator assigned
     *
     * @param host host domain indicator string
     */
    MalformedResourceException(String host) {
        super(String.format("%s is not a valid host domain", host));
    }

}

package suncertify.db;

/**
 * Exception thrown when record creation is attempted with already existing key.
 *
 * @author Emmanuel
 */
public class DuplicateKeyException extends Exception {

    /**
     * The description about the exception.
     */
    public final String description;

    /**
     * Default Constructor. <code>this.description = "Duplicate Key";</code>
     */
    public DuplicateKeyException() {
        this.description = "Duplicate Key";
    }

    /**
     * @param desc the description about this exception
     */
    public DuplicateKeyException(String desc) {
        this.description = desc;
    }

    /**
     * @return error description
     */
    @Override
    public String getMessage() {
        return this.description;
    }

}
